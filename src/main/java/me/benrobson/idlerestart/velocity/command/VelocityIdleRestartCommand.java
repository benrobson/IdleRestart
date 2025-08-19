package me.benrobson.idlerestart.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.benrobson.idlerestart.IdleRestartCore;
import me.benrobson.idlerestart.platform.PlatformAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.stream.Collectors;

public class VelocityIdleRestartCommand implements SimpleCommand {

    private final IdleRestartCore core;
    private final PlatformAdapter platform;

    public VelocityIdleRestartCommand(IdleRestartCore core, PlatformAdapter platform) {
        this.core = core;
        this.platform = platform;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource source = invocation.source();

        if (!source.hasPermission("idlerestart.use")) { // Using the same permission node
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &cYou do not have permission to use this command."));
            return;
        }

        if (args.length >= 2 && "--force".equalsIgnoreCase(args[0])) {
            try {
                int forceMinutes = Integer.parseInt(args[1]);
                if (forceMinutes <= 0) {
                    source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &cPlease specify a positive number of minutes."));
                    return;
                }
                source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &aForcing proxy restart in " + forceMinutes + " minutes."));
                core.forceRestart(forceMinutes);
            } catch (NumberFormatException e) {
                source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &cInvalid number format for minutes. Usage: /vidlerestart --force <minutes>"));
            }
        } else if (args.length == 2) {
            try {
                int idleMinutes = Integer.parseInt(args[0]);
                int numberOfPlayers = Integer.parseInt(args[1]);

                if (idleMinutes <= 0 || numberOfPlayers < 0) {
                    source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &cIdle minutes must be positive, and number of players must be non-negative."));
                    return;
                }

                core.setIdleMinutes(idleMinutes);
                core.setNumberOfPlayers(numberOfPlayers);

                source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &aIdleRestart configured. Idle time: " + idleMinutes + " minutes, Player threshold: " + numberOfPlayers + "."));
                core.startIdleCheck();
            } catch (NumberFormatException e) {
                source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &cInvalid number format. Usage: /vidlerestart <idleMinutes> <numberOfPlayers>"));
            }
        } else {
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &cUsage: /vidlerestart <idleMinutes> <numberOfPlayers> OR /vidlerestart --force <minutes>"));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            return List.of("--force", "<idleMinutes>");
        }
        if (args.length == 1) {
            if ("--force".equalsIgnoreCase(args[0])) {
                return List.of("<minutes>");
            } else {
                 // Could suggest player counts after idleMinutes
                try {
                    Integer.parseInt(args[0]); // check if it's a number
                    return List.of("<numberOfPlayers>");
                } catch (NumberFormatException e) {
                    return List.of("<idleMinutes>", "--force").stream()
                        .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
                }
            }
        }
        if (args.length == 2) {
             if ("--force".equalsIgnoreCase(args[0])) {
                return List.of("5", "10", "15"); // Suggest some common force times
            } else {
                // Suggest common player counts
                return List.of("0", "1", "5");
            }
        }
        return List.of(); // No suggestions for other cases
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("idlerestart.use");
    }
}
