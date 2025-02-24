package me.benrobson.idlerestart.events;

import me.benrobson.idlerestart.IdleRestartMain;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {
    private final IdleRestartMain plugin;

    public PlayerJoinListener(IdleRestartMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.isRestarting()) {
            plugin.cancelRestart();
            Bukkit.getConsoleSender().sendMessage("[IdleRestart] Player joined. Restart delayed.");

            // Schedule a new restart check after the delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.startIdleCheck();
                }
            }.runTaskLater(plugin, plugin.getJoinDelayMinutes() * 60 * 20L);
        }
    }
}
