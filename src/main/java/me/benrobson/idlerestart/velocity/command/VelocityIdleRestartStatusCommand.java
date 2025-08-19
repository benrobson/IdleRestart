package me.benrobson.idlerestart.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.benrobson.idlerestart.IdleRestartCore;
import me.benrobson.idlerestart.platform.PlatformAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &cYou do not have permission to use this command."));
            return;
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

            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &eProxy restart is currently pending."));
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &e -> Initiated " + pendingSinceMinutes + " minutes ago."));
            if (remainingTimeMillis > 0) {
                source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &e -> Time until restart: " + remainingMinutes + "m " + remainingSeconds + "s."));
            } else {
                source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &e -> Restart is imminent or slightly overdue."));
            }
        } else {
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(platform.getIdlePrefix() + " &aNo proxy restart is currently pending."));
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
}
