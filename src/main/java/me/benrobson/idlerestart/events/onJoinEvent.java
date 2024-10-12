package me.benrobson.idlerestart.events;

import me.benrobson.idlerestart.IdleRestartMain;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class onJoinEvent implements Listener {
    IdleRestartMain plugin;
    public onJoinEvent(IdleRestartMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.isRestarting) {
            plugin.cancelRestart();
            Bukkit.getConsoleSender().sendMessage("[IdleRestart] Player joined. Restart delayed.");
        }
    }
}
