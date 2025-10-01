package me.benrobson.idlerestart.platform;

import me.benrobson.idlerestart.IdleRestartMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.stream.Collectors;

public class BukkitPlatformAdapter implements PlatformAdapter {
    private final IdleRestartMain plugin;

    public BukkitPlatformAdapter(IdleRestartMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public void info(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void warning(String message) {
        plugin.getLogger().warning(message);
    }

    @Override
    public void severe(String message) {
        plugin.getLogger().severe(message);
    }

    @Override
    public int getOnlinePlayerCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    @Override
    public Collection<PlayerAdapter> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(BukkitPlayerAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public SchedulerTask runTaskLater(Runnable task, long delayTicks) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        return new BukkitSchedulerTask(bukkitTask);
    }

    @Override
    public SchedulerTask runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        return new BukkitSchedulerTask(bukkitTask);
    }

    @Override
    public void broadcastMessage(String message) {
        Bukkit.broadcastMessage(message);
    }

    @Override
    public void playSound(PlayerAdapter playerAdapter, String soundName, float volume, float pitch) {
        if (playerAdapter instanceof BukkitPlayerAdapter) {
            Player player = ((BukkitPlayerAdapter) playerAdapter).getBukkitPlayer();
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException e) {
                warning("Invalid sound name in config: " + soundName);
            }
        }
    }

    @Override
    public void shutdown() {
        Bukkit.shutdown();
    }

    @Override
    public String getIdlePrefix() {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("idle-prefix", "[&8[&6IR🔙&8]&r]"));
    }

    @Override
    public boolean isBroadcastRestart() {
        return plugin.getConfig().getBoolean("broadcast-restart", true);
    }

    @Override
    public String getRestartSoundName() {
        return plugin.getConfig().getString("restart-sound", "ENTITY_PLAYER_LEVELUP");
    }

    @Override
    public int getIdleMinutes() {
        return plugin.getConfig().getInt("idle-minutes", 10);
    }

    @Override
    public void setIdleMinutes(int minutes) {
        plugin.getConfig().set("idle-minutes", minutes);
        plugin.saveConfig(); // Persist change
        // plugin.loadConfig(); // Reload internal state if necessary, or update directly
    }

    @Override
    public int getNumberOfPlayers() {
        return plugin.getConfig().getInt("number-of-players", 0);
    }

    @Override
    public void setNumberOfPlayers(int players) {
        plugin.getConfig().set("number-of-players", players);
        plugin.saveConfig();
        // plugin.loadConfig();
    }

    @Override
    public int getJoinDelayMinutes() {
        return plugin.getConfig().getInt("join-delay-minutes", 5);
    }

    @Override
    public void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    @Override
    public void reloadConfig() {
        plugin.reloadConfig();
        // Need to update the main plugin's fields as well, or have IdleRestartCore use adapter for these
    }

    @Override
    public boolean isScheduledRestartEnabled() {
        return plugin.getConfig().getBoolean("scheduled-restarts.enabled", false);
    }

    @Override
    public java.util.List<String> getScheduledRestartTimes() {
        return plugin.getConfig().getStringList("scheduled-restarts.times");
    }

    @Override
    public String getScheduledRestartTimezone() {
        return plugin.getConfig().getString("scheduled-restarts.timezone", "UTC");
    }

    @Override
    public int getScheduledRestartForceDelayMinutes() {
        return plugin.getConfig().getInt("scheduled-restarts.force-delay-minutes", 15);
    }

    @Override
    public void reload() {
        plugin.reload();
    }

    @Override
    public boolean isDiscordWebhookEnabled() {
        return plugin.getConfig().getBoolean("discord.enabled", false);
    }

    @Override
    public String getDiscordWebhookUrl() {
        return plugin.getConfig().getString("discord.webhook-url", "");
    }

    @Override
    public String getDiscordServerName() {
        return plugin.getConfig().getString("discord.server-name", "My Server");
    }

    @Override
    public void callEvent(Object event) {
        if (event instanceof org.bukkit.event.Event) {
            Bukkit.getPluginManager().callEvent((org.bukkit.event.Event) event);
        }
    }
}
