package me.benrobson.idlerestart.platform;

import com.google.inject.Inject;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.Scheduler;
import me.benrobson.idlerestart.velocity.VelocityIdleRestartMain; // Assuming this will be the main class
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VelocityPlatformAdapter implements PlatformAdapter {
    private final ProxyServer proxy;
    private final Logger logger;
    private final VelocityIdleRestartMain plugin; // To access config values

    @Inject // If this class is Guice-managed in Velocity context
    public VelocityPlatformAdapter(ProxyServer proxy, Logger logger, VelocityIdleRestartMain plugin) {
        this.proxy = proxy;
        this.logger = logger;
        this.plugin = plugin;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warning(String message) {
        logger.warn(message);
    }

    @Override
    public void severe(String message) {
        logger.error(message);
    }

    @Override
    public int getOnlinePlayerCount() {
        return proxy.getPlayerCount();
    }

    @Override
    public Collection<PlayerAdapter> getOnlinePlayers() {
        return proxy.getAllPlayers().stream()
                .map(VelocityPlayerAdapter::new)
                .collect(Collectors.toList());
    }

    private VelocitySchedulerTask createAndRegisterVelocityTask(Runnable task, boolean repeating) {
        VelocitySchedulerTask platformTask = new VelocitySchedulerTask(task);
        Runnable runnable = () -> {
            if (!platformTask.isCancelled()) {
                task.run();
            }
            if (!repeating && !platformTask.isCancelled()) { // For runTaskLater, mark as cancelled after one run
                platformTask.cancel(); // Logically, not a Velocity cancel.
            }
        };
        return platformTask;
    }


    @Override
    public SchedulerTask runTaskLater(Runnable task, long delayTicks) {
        // Velocity uses durations. Bukkit ticks are 20 per second.
        long delayMillis = delayTicks * 50;
        VelocitySchedulerTask platformTask = createAndRegisterVelocityTask(task, false);
        com.velocitypowered.api.scheduler.ScheduledTask velocityScheduledTask = proxy.getScheduler().buildTask(plugin, platformTask.getOriginalTask())
                .delay(Duration.ofMillis(delayMillis))
                .schedule();
        platformTask.setActualTask(velocityScheduledTask);
        return platformTask;
    }

    @Override
    public SchedulerTask runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        long delayMillis = delayTicks * 50;
        long periodMillis = periodTicks * 50;
        VelocitySchedulerTask platformTask = createAndRegisterVelocityTask(task, true);

        com.velocitypowered.api.scheduler.ScheduledTask velocityScheduledTask = proxy.getScheduler().buildTask(plugin, platformTask.getOriginalTask())
                .delay(Duration.ofMillis(delayMillis))
                .repeat(Duration.ofMillis(periodMillis))
                .schedule();
        platformTask.setActualTask(velocityScheduledTask);
        return platformTask;
    }

    @Override
    public void broadcastMessage(String message) {
        // Velocity uses Adventure components
        Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        proxy.sendMessage(textComponent); // Broadcasts to all players with permission, or use getAllPlayers and send individually
    }

    @Override
    public void playSound(PlayerAdapter playerAdapter, String soundName, float volume, float pitch) {
        // Velocity is a proxy, it cannot directly play sounds on the client.
        // This can be a no-op or log a message.
        // info("Sound playing is not supported on Velocity (" + soundName + ")");
    }

    @Override
    public void shutdown() {
        info("Initiating Velocity proxy shutdown...");
        proxy.shutdown(LegacyComponentSerializer.legacyAmpersand().deserialize(getIdlePrefix() + " Server is restarting."));
    }

    // Config methods will delegate to the main Velocity plugin class
    @Override
    public String getIdlePrefix() {
        return plugin.getConfigManager().getIdlePrefix();
    }

    @Override
    public boolean isBroadcastRestart() {
        return plugin.getConfigManager().isBroadcastRestart();
    }

    @Override
    public String getRestartSoundName() {
        // Sounds are not really supported, but we need to implement the interface
        return plugin.getConfigManager().getRestartSound();
    }

    @Override
    public int getIdleMinutes() {
        return plugin.getConfigManager().getIdleMinutes();
    }

    @Override
    public void setIdleMinutes(int minutes) {
        plugin.getConfigManager().setIdleMinutes(minutes);
        // Saving should be handled by ConfigManager
    }

    @Override
    public int getNumberOfPlayers() {
        return plugin.getConfigManager().getNumberOfPlayers();
    }

    @Override
    public void setNumberOfPlayers(int players) {
        plugin.getConfigManager().setNumberOfPlayers(players);
    }

    @Override
    public int getJoinDelayMinutes() {
        return plugin.getConfigManager().getJoinDelayMinutes();
    }

    @Override
    public void saveDefaultConfig() {
        // This will be handled by the Velocity plugin's config loading mechanism
        plugin.getConfigManager().saveDefaultConfig();
    }

    @Override
    public void reloadConfig() {
        plugin.getConfigManager().loadConfig();
    }
}
