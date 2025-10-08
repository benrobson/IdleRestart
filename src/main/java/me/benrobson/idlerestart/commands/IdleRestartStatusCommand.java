package me.benrobson.idlerestart.commands;

import me.benrobson.idlerestart.IdleRestartCore;
import me.benrobson.idlerestart.platform.PlatformAdapter;
import me.benrobson.idlerestart.IdleRestartCore.RestartType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.concurrent.TimeUnit;

public class IdleRestartStatusCommand implements CommandExecutor {
    private final IdleRestartCore core;
    private final PlatformAdapter platform;

    public IdleRestartStatusCommand(IdleRestartCore core, PlatformAdapter platform) {
        this.core = core;
        this.platform = platform;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("idlerestart.status")) {
            sender.sendMessage(platform.getIdlePrefix() + " You do not have permission to use this command.");
            return true;
        }

        if (core.isRestartPending()) {
            long initiatedTimeMillis = core.getRestartInitiatedTimeMillis();
            long actualRestartTimeMillis = core.getActualRestartTimeMillis();
            long currentTimeMillis = System.currentTimeMillis();

            long pendingSinceMillis = currentTimeMillis - initiatedTimeMillis;
            long pendingSinceMinutes = TimeUnit.MILLISECONDS.toMinutes(pendingSinceMillis);

            long remainingTimeMillis = actualRestartTimeMillis - currentTimeMillis;
            long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMillis);
            long remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(remainingTimeMillis) % 60;

            RestartType restartType = core.getCurrentRestartType();

            sender.sendMessage(platform.getIdlePrefix() + " Server restart is currently pending.");
            sender.sendMessage(platform.getIdlePrefix() + " -> Initiated " + pendingSinceMinutes + " minutes ago.");
            sender.sendMessage(platform.getIdlePrefix() + " -> Restart type: " + formatRestartType(restartType) + ".");
            if (remainingTimeMillis > 0) {
                sender.sendMessage(platform.getIdlePrefix() + " -> Time until restart: " + remainingMinutes + "m " + remainingSeconds + "s.");
            } else {
                sender.sendMessage(platform.getIdlePrefix() + " -> Restart is imminent or slightly overdue.");
            }
            if (restartType == RestartType.FORCED) {
                sender.sendMessage(platform.getIdlePrefix() + " -> This restart is forced and will not be cancelled by player joins.");
            }
        } else {
            sender.sendMessage(platform.getIdlePrefix() + " No server restart is currently pending.");
            if (core.isScheduledRestartForcePending()) {
                long forceTimeMillis = core.getScheduledForceCheckTimeMillis();
                long currentTimeMillis = System.currentTimeMillis();
                long remainingForceMillis = Math.max(forceTimeMillis - currentTimeMillis, 0);
                long remainingForceMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingForceMillis);
                long remainingForceSeconds = TimeUnit.MILLISECONDS.toSeconds(remainingForceMillis) % 60;
                sender.sendMessage(platform.getIdlePrefix() + " A scheduled restart force check is pending.");
                sender.sendMessage(platform.getIdlePrefix() + " -> Time until escalation: " + remainingForceMinutes + "m " + remainingForceSeconds + "s.");
            }
        }
        return true;
    }

    private String formatRestartType(RestartType type) {
        String formatted = type.name().toLowerCase().replace('_', ' ');
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }
}
