package me.benrobson.idlerestart.events;

import me.benrobson.idlerestart.IdleRestartCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final IdleRestartCore core;

    public PlayerJoinListener(IdleRestartCore core) {
        this.core = core;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // The core logic for handling player joins is now in IdleRestartCore
        core.onPlayerJoin();
    }
}
