package me.benrobson.idlerestart.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.benrobson.idlerestart.IdleRestartCore;
import me.benrobson.idlerestart.platform.PlatformAdapter;
import me.benrobson.idlerestart.IdleRestartCore.RestartType;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class VelocityIdleRestartStatusCommand implements SimpleCommand {

    private final IdleRestartCore core;
    private final PlatformAdapter platform;

    public VelocityIdleRestartStatusCommand(IdleRestartCore core, PlatformAdapter platform) {
        this.core = core;
        this.platform = platform;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!source.hasPermission("idlerestart.status")) { // Using the same permission node
            sendMessage(source, platform.getIdlePrefix() + " &cYou do not have permission to use this command.");
            return;
        }

        String prefix = platform.getIdlePrefix();
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

            sendMessage(source, prefix + " &eProxy restart is currently pending.");
            sendMessage(source, prefix + " &e -> Initiated " + pendingSinceMinutes + " minutes ago.");
            sendMessage(source, prefix + " &e -> Restart type: " + formatRestartType(restartType) + ".");
            if (remainingTimeMillis > 0) {
                sendMessage(source, prefix + " &e -> Time until restart: " + remainingMinutes + "m " + remainingSeconds + "s.");
            } else {
                sendMessage(source, prefix + " &e -> Restart is imminent or slightly overdue.");
            }
            if (restartType == RestartType.FORCED) {
                sendMessage(source, prefix + " &c -> This restart is forced and will not be cancelled by player joins.");
            }
        } else {
            sendMessage(source, prefix + " &aNo proxy restart is currently pending.");
            if (core.isScheduledRestartForcePending()) {
                long forceTimeMillis = core.getScheduledForceCheckTimeMillis();
                long currentTimeMillis = System.currentTimeMillis();
                long remainingForceMillis = Math.max(forceTimeMillis - currentTimeMillis, 0);
                long remainingForceMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingForceMillis);
                long remainingForceSeconds = TimeUnit.MILLISECONDS.toSeconds(remainingForceMillis) % 60;
                sendMessage(source, prefix + " &eA scheduled restart force check is pending.");
                sendMessage(source, prefix + " &e -> Time until escalation: " + remainingForceMinutes + "m " + remainingForceSeconds + "s.");
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of(); // No arguments for this command
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("idlerestart.status");
    }

    private void sendMessage(CommandSource source, String message) {
        source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    private String formatRestartType(RestartType type) {
        String formatted = type.name().toLowerCase().replace('_', ' ');
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }
}
