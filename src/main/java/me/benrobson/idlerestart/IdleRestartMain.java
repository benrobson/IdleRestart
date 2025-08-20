package me.benrobson.idlerestart;

import me.benrobson.idlerestart.commands.IdleRestartCommand;
import me.benrobson.idlerestart.commands.IdleRestartStatusCommand;
import me.benrobson.idlerestart.events.PlayerJoinListener;
import me.benrobson.idlerestart.platform.BukkitPlatformAdapter;
import me.benrobson.idlerestart.platform.PlatformAdapter;
import me.benrobson.idlerestart.api.IdleRestartAPI;
import me.benrobson.idlerestart.scheduler.ScheduledRestartManager;
import me.benrobson.idlerestart.webhook.DiscordWebhookManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;

public class IdleRestartMain extends JavaPlugin {
    private IdleRestartCore core;
    private PlatformAdapter platformAdapter;
    private ScheduledRestartManager scheduledRestartManager;
    private DiscordWebhookManager discordWebhookManager;
    private IdleRestartAPI api;

    @Override
    public void onEnable() {
        // Configuration is still managed by the Bukkit Plugin part for now
        saveDefaultConfig(); // Ensures config.yml exists
        // No need to call loadConfig() here as PlatformAdapter will read directly or core will trigger it.

        this.platformAdapter = new BukkitPlatformAdapter(this);
        this.discordWebhookManager = new DiscordWebhookManager(this.platformAdapter);
        this.core = new IdleRestartCore(this.platformAdapter, this.discordWebhookManager);
        this.core.initialize(); // Core can now access config via adapter

        // Register commands and listeners
        getCommand("idlerestart").setExecutor(new IdleRestartCommand(this.core, this.platformAdapter));
        getCommand("idlerestartstatus").setExecutor(new IdleRestartStatusCommand(this.core, this.platformAdapter));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this.core), this);

        this.api = new IdleRestartAPI(this.core);
        getServer().getServicesManager().register(IdleRestartAPI.class, this.api, this, ServicePriority.Normal);

        this.scheduledRestartManager = new ScheduledRestartManager(this.core, this.platformAdapter);
        this.scheduledRestartManager.scheduleRestarts();

        getLogger().info("IdleRestart plugin enabled. Idle check will start upon command configuration.");
        // The comment "// Do not start idle check automatically on server start" is respected.
        // core.startIdleCheck() is not called here.
    }

    @Override
    public void onDisable() {
        if (core != null) {
            core.shutdown();
        }
        getLogger().info("IdleRestart plugin disabled.");
    }

    // Methods that were previously here for config access or direct action
    // are now either handled by IdleRestartCore or BukkitPlatformAdapter.
    // For example, getConfig() is available from JavaPlugin, and BukkitPlatformAdapter uses it.

    // If other parts of the plugin (e.g. commands, listeners not yet refactored)
    // need direct access to core or platform adapter:
    public IdleRestartCore getCore() {
        return core;
    }

    public PlatformAdapter getPlatformAdapter() {
        return platformAdapter;
    }

    public void reload() {
        platformAdapter.reloadConfig();
        core.reload(); // Reloads idle check

        if (scheduledRestartManager != null) {
            scheduledRestartManager.scheduleRestarts(); // This will cancel old and schedule new
        }
        getLogger().info("IdleRestart configuration and scheduled restarts have been reloaded.");
    }

    public IdleRestartAPI getApi() {
        return api;
    }
}
