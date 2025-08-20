package me.benrobson.idlerestart.commands;

import me.benrobson.idlerestart.IdleRestartCore;
import me.benrobson.idlerestart.platform.PlatformAdapter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class IdleRestartCommand implements CommandExecutor {
    private final IdleRestartCore core;
    private final PlatformAdapter platform;

    public IdleRestartCommand(IdleRestartCore core, PlatformAdapter platform) {
        this.core = core;
        this.platform = platform;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("idlerestart.use")) {
            sender.sendMessage(platform.getIdlePrefix() + " You do not have permission to use this command.");
            return true;
        }

        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("idlerestart.reload")) {
                sender.sendMessage(platform.getIdlePrefix() + " You do not have permission to use this command.");
                return true;
            }
            platform.reload();
            sender.sendMessage(platform.getIdlePrefix() + " IdleRestart configuration reloaded.");
        } else if (args.length >= 2 && "--force".equalsIgnoreCase(args[0])) { // Use equalsIgnoreCase for flexibility
            try {
                int forceMinutes = Integer.parseInt(args[1]);
                if (forceMinutes <= 0) {
                    sender.sendMessage(platform.getIdlePrefix() + " Please specify a positive number of minutes.");
                    return true;
                }
                sender.sendMessage(platform.getIdlePrefix() + " Forcing server restart in " + forceMinutes + " minutes.");
                core.forceRestart(forceMinutes);
            } catch (NumberFormatException e) {
                sender.sendMessage(platform.getIdlePrefix() + " Invalid number format for minutes. Usage: /idlerestart --force <minutes>");
            }
        } else if (args.length == 2) {
            try {
                int idleMinutes = Integer.parseInt(args[0]);
                int numberOfPlayers = Integer.parseInt(args[1]);

                if (idleMinutes <= 0 || numberOfPlayers < 0) {
                    sender.sendMessage(platform.getIdlePrefix() + " Idle minutes must be positive, and number of players must be non-negative.");
                    return true;
                }

                core.setIdleMinutes(idleMinutes); // These methods in core now also call platform.set...
                core.setNumberOfPlayers(numberOfPlayers);
                // platform.setIdleMinutes(idleMinutes); // Let core handle this to potentially restart task
                // platform.setNumberOfPlayers(numberOfPlayers);

                sender.sendMessage(platform.getIdlePrefix() + " IdleRestart configured. Idle time: " + idleMinutes + " minutes, Player threshold: " + numberOfPlayers + ".");
                core.startIdleCheck(); // This will also (re)start the idle check task
            } catch (NumberFormatException e) {
                sender.sendMessage(platform.getIdlePrefix() + " Invalid number format. Usage: /idlerestart <idleMinutes> <numberOfPlayers>");
            }
        } else {
            sender.sendMessage(platform.getIdlePrefix() + " Usage: /idlerestart <idleMinutes> <numberOfPlayers> OR /idlerestart --force <minutes> OR /idlerestart reload");
        }
        return true;
    }
}
