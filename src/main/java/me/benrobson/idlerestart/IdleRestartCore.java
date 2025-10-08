package me.benrobson.idlerestart;

import me.benrobson.idlerestart.platform.PlatformAdapter;
import me.benrobson.idlerestart.platform.PlayerAdapter;
import me.benrobson.idlerestart.platform.SchedulerTask;
import me.benrobson.idlerestart.webhook.DiscordWebhookManager;

public class IdleRestartCore {
    private final PlatformAdapter platform;
    private final DiscordWebhookManager webhookManager;
    private boolean isRestarting = false;
    private long restartTimeMillis = 0; // Time when the restart was initiated
    private long actualRestartTimeMillis = 0; // Time when server will actually restart
    private SchedulerTask currentShutdownTask;
    private SchedulerTask idleCheckTask;
    private SchedulerTask scheduledForceTask;
    private long scheduledForceCheckTimeMillis = 0L;
    private boolean scheduledRestartForcePending = false;
    private RestartType currentRestartType = RestartType.NONE;

    public enum RestartType {
        NONE,
        IDLE,
        SCHEDULED,
        FORCED
    }

    public IdleRestartCore(PlatformAdapter platform, DiscordWebhookManager webhookManager) {
        this.platform = platform;
        this.webhookManager = webhookManager;
    }

    public void initialize() {
        // Configuration values are now obtained via platform adapter
        // No automatic start of idle check unless explicitly called
        platform.info("IdleRestartCore initialized. Idle check will start when configured via command.");
    }

    public void startIdleCheck() {
        if (idleCheckTask != null && !idleCheckTask.isCancelled()) {
            idleCheckTask.cancel(); // Cancel existing task
        }
        platform.info("Starting idle check. Server will be checked every minute.");
        idleCheckTask = platform.runTaskTimer(() -> {
            if (platform.getOnlinePlayerCount() <= platform.getNumberOfPlayers() && !isRestarting) {
                platform.info("Server is idle. Scheduling restart.");
                scheduleRestart(platform.getIdleMinutes(), "due to inactivity", RestartType.IDLE);
            }
        }, 0L, 20L * 60L); // Check every minute
    }

    public void scheduleRestart(int minutes, String reason) {
        scheduleRestart(minutes, reason, RestartType.IDLE);
    }

    public void scheduleRestart(int minutes, String reason, RestartType type) {
        if (minutes <= 0) {
            platform.warning("Attempted to schedule a restart with a non-positive delay (" + minutes + "). Defaulting to 1 minute.");
            minutes = 1;
        }

        long now = System.currentTimeMillis();
        long requestedRestartTimeMillis = now + (minutes * 60L * 1000L);
        if (isRestarting && actualRestartTimeMillis <= requestedRestartTimeMillis && type != RestartType.FORCED) {
            platform.info("Restart already scheduled and sooner or at the same time.");
            return;
        }

        cancelCurrentShutdownTask();
        cancelScheduledForceTask();

        isRestarting = true;
        restartTimeMillis = now;
        actualRestartTimeMillis = requestedRestartTimeMillis;
        currentRestartType = type;

        if (platform.isBroadcastRestart()) {
            String message = platform.getIdlePrefix() + " Server will restart in " + minutes + " minutes " + reason + ".";
            platform.broadcastMessage(message);
            String soundName = platform.getRestartSoundName();
            if (soundName != null && !soundName.isEmpty()) {
                for (PlayerAdapter player : platform.getOnlinePlayers()) {
                    platform.playSound(player, soundName, 1.0f, 1.0f);
                }
            }
        }

        if (type == RestartType.SCHEDULED) {
            scheduleScheduledForceCheck();
        }

        currentShutdownTask = platform.runTaskLater(() -> {
            if (isRestarting) {
                platform.info("Executing scheduled server shutdown.");
                platform.shutdown();
            }
        }, minutes * 60L * 20L);
        platform.info("Server restart scheduled in " + minutes + " minutes.");
        webhookManager.sendRestartNotification(reason);
        platform.fireRestartScheduledEvent(minutes, reason);
    }

    public void forceRestart(int minutes) {
        platform.info("Forcing server restart in " + minutes + " minutes.");
        scheduleRestart(minutes, "due to forced restart", RestartType.FORCED);
        platform.fireRestartForcedEvent(minutes);
    }

    private void cancelCurrentShutdownTask() {
        if (currentShutdownTask != null && !currentShutdownTask.isCancelled()) {
            currentShutdownTask.cancel();
            currentShutdownTask = null;
        }
    }

    private void cancelScheduledForceTask() {
        if (scheduledForceTask != null && !scheduledForceTask.isCancelled()) {
            scheduledForceTask.cancel();
        }
        scheduledForceTask = null;
        scheduledForceCheckTimeMillis = 0L;
        scheduledRestartForcePending = false;
    }

    private void scheduleScheduledForceCheck() {
        int delayMinutes = platform.getScheduledRestartForceDelayMinutes();
        if (delayMinutes <= 0) {
            platform.info("Scheduled restart force delay is zero or negative; skipping force escalation.");
            scheduledRestartForcePending = false;
            scheduledForceCheckTimeMillis = 0L;
            return;
        }

        scheduledRestartForcePending = true;
        long now = System.currentTimeMillis();
        scheduledForceCheckTimeMillis = now + (delayMinutes * 60L * 1000L);
        platform.info("Scheduled restart will be forced if the player threshold is still met in " + delayMinutes + " minutes.");

        scheduledForceTask = platform.runTaskLater(() -> handleScheduledForceCheck(delayMinutes), delayMinutes * 60L * 20L);
    }

    private void handleScheduledForceCheck(int delayMinutes) {
        scheduledForceTask = null;
        if (!scheduledRestartForcePending) {
            platform.info("Scheduled restart force escalation no longer required.");
            scheduledForceCheckTimeMillis = 0L;
            return;
        }

        scheduledRestartForcePending = false;
        scheduledForceCheckTimeMillis = 0L;

        int onlinePlayers = platform.getOnlinePlayerCount();
        int threshold = platform.getNumberOfPlayers();
        if (onlinePlayers <= threshold) {
            platform.info("Scheduled restart threshold not met after " + delayMinutes + " minutes (" + onlinePlayers + " online, threshold " + threshold + "). Initiating forced restart.");
            forceRestart(1);
        } else {
            platform.info("Scheduled restart threshold met after " + delayMinutes + " minutes (" + onlinePlayers + " online, threshold " + threshold + "). Forced restart not required.");
        }
    }

    public void cancelRestart(String reason) {
        if (isRestarting) {
            RestartType cancelledType = currentRestartType;
            isRestarting = false;
            currentRestartType = RestartType.NONE;
            restartTimeMillis = 0L;
            actualRestartTimeMillis = 0;
            cancelCurrentShutdownTask();
            if (cancelledType != RestartType.SCHEDULED) {
                cancelScheduledForceTask();
            } else if (scheduledRestartForcePending) {
                platform.info("Scheduled restart cancelled, but force check remains pending.");
            }
            platform.info("Server restart cancelled. Reason: " + reason);
            if (platform.isBroadcastRestart()) {
                platform.broadcastMessage(platform.getIdlePrefix() + " Server restart has been cancelled. Reason: " + reason);
            }
            platform.fireRestartCancelledEvent(reason);
        }
    }

    public void onPlayerJoin() {
        if (isRestarting) {
            if (currentRestartType == RestartType.FORCED) {
                platform.info("Player joined while a forced restart is pending. Restart will continue as scheduled.");
                return;
            }

            cancelRestart("player joined");
            platform.info("Player joined. Restart cancelled. Scheduling new idle check after " + platform.getJoinDelayMinutes() + " minutes.");

            // Schedule a new idle check after the configured delay
            // This is different from original: original would restart the idle check immediately after cancelling.
            // The description says "Delays restart when a player joins the server."
            // And the PlayerJoinListener says "Schedule a new restart check after the delay"
            // This seems more aligned with "delaying" the restart process.
            platform.runTaskLater(this::startIdleCheck, platform.getJoinDelayMinutes() * 60 * 20L);
        } else if (scheduledRestartForcePending) {
            platform.info("Player joined while a scheduled restart force check is pending. Restart remains cancelled unless the idle threshold is met when the force check runs.");
        }
    }

    public boolean isRestartPending() {
        return isRestarting;
    }

    public long getRestartInitiatedTimeMillis() {
        return restartTimeMillis;
    }

    public long getActualRestartTimeMillis() {
        return actualRestartTimeMillis;
    }

    public RestartType getCurrentRestartType() {
        return currentRestartType;
    }

    public boolean isScheduledRestartForcePending() {
        return scheduledRestartForcePending;
    }

    public long getScheduledForceCheckTimeMillis() {
        return scheduledRestartForcePending ? scheduledForceCheckTimeMillis : 0L;
    }

    public void setIdleMinutes(int minutes) {
        platform.setIdleMinutes(minutes);
        // If an idle check is running, we might want to restart it to pick up new values,
        // or assume it will pick them up on the next check.
        // For now, let's restart it to make changes immediate.
        if (idleCheckTask != null && !idleCheckTask.isCancelled()) {
             platform.info("Idle minutes changed. Restarting idle check.");
             startIdleCheck(); // This will cancel the old and start a new one
        }
    }

    public void setNumberOfPlayers(int players) {
        platform.setNumberOfPlayers(players);
        if (idleCheckTask != null && !idleCheckTask.isCancelled()) {
            platform.info("Number of players for idle check changed. Restarting idle check.");
            startIdleCheck();
        }
    }

    public void shutdown() {
        platform.info("IdleRestartCore shutting down. Cancelling tasks.");
        if (idleCheckTask != null && !idleCheckTask.isCancelled()) {
            idleCheckTask.cancel();
        }
        cancelCurrentShutdownTask();
        cancelScheduledForceTask();
    }

    public void reload() {
        // The ScheduledRestartManager will be reloaded from the main plugin class
        // as it's not directly managed by the core.
        // However, if there were any core-specific caches or settings from config,
        // they would be reloaded here.
        platform.info("IdleRestartCore reloaded.");
        // For example, if idle check is running, we might want to restart it
        // to pick up new values if they were changed.
        if (idleCheckTask != null && !idleCheckTask.isCancelled()) {
            startIdleCheck();
        }
    }
}
