package me.benrobson.idlerestart.commands;

import me.benrobson.idlerestart.IdleRestartMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class IdleRestartCommand implements CommandExecutor {
    private final IdleRestartMain plugin;

    public IdleRestartCommand(IdleRestartMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
            sender.sendMessage("This command can only be executed from the console.");
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("start")) {
                if (plugin.isRestarting) {
                    sender.sendMessage("Idle restart is already in progress.");
                    return true;
                }
                plugin.startRestartCountdown();
                sender.sendMessage("Idle restart countdown started.");
                return true;
            }

            if (args[0].equalsIgnoreCase("stop")) {
                if (!plugin.isRestarting) {
                    sender.sendMessage("No idle restart in progress.");
                    return true;
                }
                plugin.stopRestartCountdown();
                sender.sendMessage("Idle restart countdown stopped.");
                return true;
            }
        }

        sender.sendMessage("Usage: /idlerestart <start|stop>");
        return true;
    }
}
