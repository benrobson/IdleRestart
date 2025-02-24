package me.benrobson.idlerestart.commands;

import me.benrobson.idlerestart.IdleRestartMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class IdleRestartStatusCommand implements CommandExecutor {
    private final IdleRestartMain plugin;

    public IdleRestartStatusCommand(IdleRestartMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("idlerestart.status")) {
            sender.sendMessage(plugin.getIdlePrefix() + " You do not have permission to use this command.");
            return true;
        }

        if (plugin.isRestarting()) {
            long pendingTimeMinutes = (System.currentTimeMillis() - plugin.getRestartTime()) / 1000 / 60;
            sender.sendMessage(plugin.getIdlePrefix() + " Restart is pending. It has been pending for " + pendingTimeMinutes + " minutes.");
        } else {
            sender.sendMessage(plugin.getIdlePrefix() + " No restart is pending.");
        }
        return true;
    }
}
