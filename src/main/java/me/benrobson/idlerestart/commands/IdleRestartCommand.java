package me.benrobson.idlerestart.commands;

import me.benrobson.idlerestart.IdleRestartMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class IdleRestartCommand implements CommandExecutor {
    private final IdleRestartMain plugin;

    public IdleRestartCommand(IdleRestartMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("idlerestart.use")) {
            sender.sendMessage(plugin.getIdlePrefix() + " You do not have permission to use this command.");
            return true;
        }

        if (args.length >= 2 && "--force".equals(args[0])) {
            try {
                int forceMinutes = Integer.parseInt(args[1]);
                sender.sendMessage(plugin.getIdlePrefix() + " Forcing server restart in " + forceMinutes + " minutes.");
                plugin.forceRestart(forceMinutes);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getIdlePrefix() + " Invalid number format. Usage: /idlerestart --force <minutes>");
            }
        } else if (args.length == 2) {
            try {
                int idleMinutes = Integer.parseInt(args[0]);
                int numberOfPlayers = Integer.parseInt(args[1]);
                plugin.setIdleMinutes(idleMinutes);
                plugin.setNumberOfPlayers(numberOfPlayers);
                sender.sendMessage(plugin.getIdlePrefix() + " IdleRestart configured with " + idleMinutes + " minutes and " + numberOfPlayers + " players.");
                plugin.startIdleCheck();
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getIdlePrefix() + " Invalid number format. Usage: /idlerestart <idleMinutes> <numberOfPlayers>");
            }
        } else {
            sender.sendMessage(plugin.getIdlePrefix() + " Usage: /idlerestart <idleMinutes> <numberOfPlayers> or /idlerestart --force <minutes>");
        }
        return true;
    }
}
