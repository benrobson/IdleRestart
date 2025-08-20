package me.benrobson.idlerestart;

import me.benrobson.idlerestart.api.events.RestartCancelledEvent;
import me.benrobson.idlerestart.api.events.RestartForcedEvent;
import me.benrobson.idlerestart.api.events.RestartScheduledEvent;
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
                scheduleRestart(platform.getIdleMinutes(), "due to inactivity");
            }
        }, 0L, 20L * 60L); // Check every minute
    }

    public void scheduleRestart(int minutes, String reason) {
        if (isRestarting && actualRestartTimeMillis < System.currentTimeMillis() + (minutes * 60 * 1000L)) {
            platform.info("Restart already scheduled and sooner or at the same time.");
            return;
        }

        cancelCurrentShutdownTask(); // Cancel any existing shutdown task

        isRestarting = true;
        restartTimeMillis = System.currentTimeMillis();
        actualRestartTimeMillis = System.currentTimeMillis() + (minutes * 60 * 1000L);

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

        currentShutdownTask = platform.runTaskLater(() -> {
            if (isRestarting) { // Re-check in case it was cancelled
                platform.info("Executing scheduled server shutdown.");
                platform.shutdown();
            }
        }, minutes * 60 * 20L); // minutes to ticks
        platform.info("Server restart scheduled in " + minutes + " minutes.");
        webhookManager.sendRestartNotification(reason);
        platform.callEvent(new RestartScheduledEvent(minutes, reason));
    }

    public void forceRestart(int minutes) {
        platform.info("Forcing server restart in " + minutes + " minutes.");
        scheduleRestart(minutes, "due to forced restart");
        platform.callEvent(new RestartForcedEvent(minutes));
    }

    private void cancelCurrentShutdownTask() {
        if (currentShutdownTask != null && !currentShutdownTask.isCancelled()) {
            currentShutdownTask.cancel();
            currentShutdownTask = null;
        }
    }

    public void cancelRestart(String reason) {
        if (isRestarting) {
            isRestarting = false;
            actualRestartTimeMillis = 0;
            cancelCurrentShutdownTask();
            platform.info("Server restart cancelled. Reason: " + reason);
            if (platform.isBroadcastRestart()) {
                platform.broadcastMessage(platform.getIdlePrefix() + " Server restart has been cancelled. Reason: " + reason);
            }
            platform.callEvent(new RestartCancelledEvent(reason));
        }
    }

    public void onPlayerJoin() {
        if (isRestarting) {
            long remainingTimeMillis = actualRestartTimeMillis - System.currentTimeMillis();
            int joinDelayMillis = platform.getJoinDelayMinutes() * 60 * 1000;

            // Only delay if the player joins before the final moments OR if joinDelay is significant enough
            // This avoids issues where a player joins 1 second before restart and it gets delayed.
            // For now, let's always delay if a restart is pending.
            cancelRestart("player joined");
            platform.info("Player joined. Restart cancelled. Scheduling new idle check after " + platform.getJoinDelayMinutes() + " minutes.");

            // Schedule a new idle check after the configured delay
            // This is different from original: original would restart the idle check immediately after cancelling.
            // The description says "Delays restart when a player joins the server."
            // And the PlayerJoinListener says "Schedule a new restart check after the delay"
            // This seems more aligned with "delaying" the restart process.
            platform.runTaskLater(this::startIdleCheck, platform.getJoinDelayMinutes() * 60 * 20L);
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
