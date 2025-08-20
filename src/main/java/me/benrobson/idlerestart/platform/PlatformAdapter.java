package me.benrobson.idlerestart.platform;

import java.util.Collection;
import java.util.UUID;

public interface PlatformAdapter {
    void info(String message);
    void warning(String message);
    void severe(String message);
    int getOnlinePlayerCount();
    Collection<PlayerAdapter> getOnlinePlayers();
    SchedulerTask runTaskLater(Runnable task, long delayTicks);
    SchedulerTask runTaskTimer(Runnable task, long delayTicks, long periodTicks);
    void broadcastMessage(String message);
    void playSound(PlayerAdapter player, String soundName, float volume, float pitch); // Sound will be tricky, Velocity has limited sound support
    void shutdown();
    String getIdlePrefix();
    boolean isBroadcastRestart();
    String getRestartSoundName();
    int getIdleMinutes();
    void setIdleMinutes(int minutes);
    int getNumberOfPlayers();
    void setNumberOfPlayers(int players);
    int getJoinDelayMinutes();
    void saveDefaultConfig();
    void reloadConfig(); // Or a method to update specific config values
    // Config access might need to be part of the adapter or a separate service
    // For now, let's assume main plugin class will handle config and pass values to adapter

    // Scheduled Restarts
    boolean isScheduledRestartEnabled();
    java.util.List<String> getScheduledRestartTimes();
    String getScheduledRestartTimezone();
    void reload();

    // Discord Webhook
    boolean isDiscordWebhookEnabled();
    String getDiscordWebhookUrl();
    String getDiscordServerName();

    // Events
    void callEvent(Object event);
}
