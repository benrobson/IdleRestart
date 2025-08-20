package me.benrobson.idlerestart.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.benrobson.idlerestart.IdleRestartCore;
import me.benrobson.idlerestart.platform.PlatformAdapter;
import me.benrobson.idlerestart.platform.VelocityPlatformAdapter;
import me.benrobson.idlerestart.scheduler.ScheduledRestartManager;
import me.benrobson.idlerestart.webhook.DiscordWebhookManager;
import me.benrobson.idlerestart.velocity.command.VelocityIdleRestartCommand;
import me.benrobson.idlerestart.velocity.command.VelocityIdleRestartStatusCommand;
import me.benrobson.idlerestart.velocity.listener.VelocityPlayerListener;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "idlerestart",
        name = "IdleRestart",
        version = "1.0.0-SNAPSHOT", // Match your project version
        description = "Restarts the proxy after a set amount of minutes with low player count.",
        authors = {"Ben Robson", "Jules (AI Assistant)"}
)
public class VelocityIdleRestartMain {

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;

    private IdleRestartCore core;
    private PlatformAdapter platformAdapter;
    private VelocityConfigManager configManager;
    private DiscordWebhookManager discordWebhookManager;
    private ScheduledRestartManager scheduledRestartManager;

    @Inject
    public VelocityIdleRestartMain(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.configManager = new VelocityConfigManager(dataDirectory, logger);
        // ConfigManager loads config upon instantiation.

        this.platformAdapter = new VelocityPlatformAdapter(proxy, logger, this);
        this.discordWebhookManager = new DiscordWebhookManager(this.platformAdapter);
        this.core = new IdleRestartCore(this.platformAdapter, this.discordWebhookManager);
        this.core.initialize(); // Core logic initialization

        // Scheduled Restarts
        this.scheduledRestartManager = new ScheduledRestartManager(this.core, this.platformAdapter);
        this.scheduledRestartManager.scheduleRestarts();

        // Register commands
        proxy.getCommandManager().register(
                proxy.getCommandManager().metaBuilder("v_idlerestart").aliases("vidlerestart").build(),
                new VelocityIdleRestartCommand(core, platformAdapter)
        );
        proxy.getCommandManager().register(
                proxy.getCommandManager().metaBuilder("v_idlerestartstatus").aliases("vidlerestartstatus").build(),
                new VelocityIdleRestartStatusCommand(core, platformAdapter)
        );

        // Register listeners
        proxy.getEventManager().register(this, new VelocityPlayerListener(core));

        logger.info("IdleRestart plugin enabled for Velocity. Idle check will start upon command configuration.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (core != null) {
            core.shutdown();
        }
        logger.info("IdleRestart plugin disabled for Velocity.");
    }

    public VelocityConfigManager getConfigManager() {
        return configManager;
    }

    public IdleRestartCore getCore() {
        return core;
    }

    public PlatformAdapter getPlatformAdapter() {
        return platformAdapter;
    }

    public void reload() {
        configManager.loadConfig();
        core.reload(); // Reloads idle check

        if (scheduledRestartManager != null) {
            scheduledRestartManager.scheduleRestarts(); // This will cancel old and schedule new
        }
        logger.info("IdleRestart configuration and scheduled restarts have been reloaded.");
    }
}
