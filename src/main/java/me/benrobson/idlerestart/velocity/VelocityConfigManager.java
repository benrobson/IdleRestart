package me.benrobson.idlerestart.velocity;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class VelocityConfigManager {
    private final Path dataDirectory;
    private final Logger logger;
    private CommentedFileConfig config;

    private static final String FILE_NAME = "idlerestart.toml";

    // Default values
    private int idleMinutes = 10;
    private int numberOfPlayers = 0;
    private boolean broadcastRestart = true;
    private String restartSound = "ENTITY_PLAYER_LEVELUP"; // Will be ignored by Velocity mostly
    private int joinDelayMinutes = 5;
    private String idlePrefix = "[&8[&6IR🔙&8]&r]"; // Legacy color codes

    // Scheduled Restarts
    private boolean scheduledRestartEnabled = false;
    private List<String> scheduledRestartTimes = Arrays.asList("04:00", "16:00");
    private String scheduledRestartTimezone = "UTC";
    private int scheduledRestartForceDelayMinutes = 15;

    // Discord Webhook
    private boolean discordWebhookEnabled = false;
    private String discordWebhookUrl = "";
    private String discordServerName = "My Server";

    public VelocityConfigManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        loadConfig();
    }

    public void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            Path configFile = dataDirectory.resolve(FILE_NAME);
            this.config = CommentedFileConfig.builder(configFile)
                    .autosave()
                    .writingMode(WritingMode.REPLACE)
                    .build();

            config.load();
            saveDefaultConfig(); // This will write defaults if keys are missing and save

            // Load values
            idleMinutes = config.getIntOrElse("idle-minutes", idleMinutes);
            numberOfPlayers = config.getIntOrElse("number-of-players", numberOfPlayers);
            broadcastRestart = config.getOrElse("broadcast-restart", broadcastRestart);
            restartSound = config.getOrElse("restart-sound", restartSound);
            joinDelayMinutes = config.getIntOrElse("join-delay-minutes", joinDelayMinutes);
            idlePrefix = config.getOrElse("idle-prefix", idlePrefix);

            // Scheduled Restarts
            scheduledRestartEnabled = config.getOrElse("scheduled-restarts.enabled", scheduledRestartEnabled);
            scheduledRestartTimes = config.getOrElse("scheduled-restarts.times", scheduledRestartTimes);
            scheduledRestartTimezone = config.getOrElse("scheduled-restarts.timezone", scheduledRestartTimezone);
            scheduledRestartForceDelayMinutes = config.getIntOrElse("scheduled-restarts.force-delay-minutes", scheduledRestartForceDelayMinutes);

            // Discord Webhook
            discordWebhookEnabled = config.getOrElse("discord.enabled", discordWebhookEnabled);
            discordWebhookUrl = config.getOrElse("discord.webhook-url", discordWebhookUrl);
            discordServerName = config.getOrElse("discord.server-name", discordServerName);

            // Ensure values are saved if they were defaulted
            config.save();

        } catch (Exception e) {
            logger.error("Failed to load or create IdleRestart configuration: " + FILE_NAME, e);
        }
    }

    public void saveDefaultConfig() {
        // Set comments and default values if not present
        config.setComment("idle-minutes", "Number of minutes to wait before restarting the server when idle");
        if (!config.contains("idle-minutes")) config.set("idle-minutes", idleMinutes);

        config.setComment("number-of-players", "Number of players below which the server is considered idle");
        if (!config.contains("number-of-players")) config.set("number-of-players", numberOfPlayers);

        config.setComment("broadcast-restart", "Whether to broadcast a message when a restart is scheduled (sound is not supported on Velocity)");
        if (!config.contains("broadcast-restart")) config.set("broadcast-restart", broadcastRestart);

        config.setComment("restart-sound", "Sound to play when a restart is broadcasted (not used by Velocity proxy)");
        if (!config.contains("restart-sound")) config.set("restart-sound", restartSound);

        config.setComment("join-delay-minutes", "Delay in minutes to restart when a player joins and cancels a pending restart");
        if (!config.contains("join-delay-minutes")) config.set("join-delay-minutes", joinDelayMinutes);

        config.setComment("idle-prefix", "Prefix for all plugin messages (uses legacy '&' color codes)");
        if (!config.contains("idle-prefix")) config.set("idle-prefix", idlePrefix);

        // Scheduled Restarts
        config.setComment("scheduled-restarts.enabled", "Enable or disable scheduled restarts");
        if (!config.contains("scheduled-restarts.enabled")) config.set("scheduled-restarts.enabled", scheduledRestartEnabled);
        config.setComment("scheduled-restarts.times", "A list of times in HH:mm format (24-hour clock) to restart the server");
        if (!config.contains("scheduled-restarts.times")) config.set("scheduled-restarts.times", scheduledRestartTimes);
        config.setComment("scheduled-restarts.timezone", "The timezone for scheduled restarts (examples: UTC, Australia/Sydney). Full list: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones");
        if (!config.contains("scheduled-restarts.timezone")) config.set("scheduled-restarts.timezone", scheduledRestartTimezone);
        config.setComment("scheduled-restarts.force-delay-minutes", "Minutes to wait before forcing a scheduled restart if the idle threshold is still met");
        if (!config.contains("scheduled-restarts.force-delay-minutes")) config.set("scheduled-restarts.force-delay-minutes", scheduledRestartForceDelayMinutes);

        // Discord Webhook
        config.setComment("discord.enabled", "Enable or disable Discord webhook notifications");
        if (!config.contains("discord.enabled")) config.set("discord.enabled", discordWebhookEnabled);
        config.setComment("discord.webhook-url", "The webhook URL from Discord");
        if (!config.contains("discord.webhook-url")) config.set("discord.webhook-url", discordWebhookUrl);
        config.setComment("discord.server-name", "The name of the server to display in the notification");
        if (!config.contains("discord.server-name")) config.set("discord.server-name", discordServerName);
    }

    private void save() {
        if (config != null) {
            config.save();
        }
    }

    // Getters
    public int getIdleMinutes() { return idleMinutes; }
    public int getNumberOfPlayers() { return numberOfPlayers; }
    public boolean isBroadcastRestart() { return broadcastRestart; }
    public String getRestartSound() { return restartSound; }
    public int getJoinDelayMinutes() { return joinDelayMinutes; }
    public String getIdlePrefix() { return idlePrefix; }
    public boolean isScheduledRestartEnabled() { return scheduledRestartEnabled; }
    public java.util.List<String> getScheduledRestartTimes() { return scheduledRestartTimes; }
    public String getScheduledRestartTimezone() { return scheduledRestartTimezone; }
    public int getScheduledRestartForceDelayMinutes() { return scheduledRestartForceDelayMinutes; }
    public boolean isDiscordWebhookEnabled() { return discordWebhookEnabled; }
    public String getDiscordWebhookUrl() { return discordWebhookUrl; }
    public String getDiscordServerName() { return discordServerName; }

    // Setters that also update the config file
    public void setIdleMinutes(int idleMinutes) {
        this.idleMinutes = idleMinutes;
        config.set("idle-minutes", idleMinutes);
        save();
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
        config.set("number-of-players", numberOfPlayers);
        save();
    }
}
