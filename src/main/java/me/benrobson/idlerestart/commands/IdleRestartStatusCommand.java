package me.benrobson.idlerestart.commands;

import me.benrobson.idlerestart.IdleRestartCore;
import me.benrobson.idlerestart.platform.PlatformAdapter;
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


            sender.sendMessage(platform.getIdlePrefix() + " Server restart is currently pending.");
            sender.sendMessage(platform.getIdlePrefix() + " -> Initiated " + pendingSinceMinutes + " minutes ago.");
            if (remainingTimeMillis > 0) {
                sender.sendMessage(platform.getIdlePrefix() + " -> Time until restart: " + remainingMinutes + "m " + remainingSeconds + "s.");
            } else {
                sender.sendMessage(platform.getIdlePrefix() + " -> Restart is imminent or slightly overdue.");
            }
        } else {
            sender.sendMessage(platform.getIdlePrefix() + " No server restart is currently pending.");
        }
        return true;
    }
}
