package me.benrobson.idlerestart;

import me.benrobson.idlerestart.commands.IdleRestartCommand;
import me.benrobson.idlerestart.commands.IdleRestartStatusCommand;
import me.benrobson.idlerestart.events.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class IdleRestartMain extends JavaPlugin {
    private int idleMinutes;
    private int numberOfPlayers;
    private boolean broadcastRestart;
    private Sound restartSound;
    private int joinDelayMinutes;
    private boolean isRestarting = false;
    private long restartTime = 0;
    private String idlePrefix;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        getCommand("idlerestart").setExecutor(new IdleRestartCommand(this));
        getCommand("idlerestartstatus").setExecutor(new IdleRestartStatusCommand(this));

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Do not start idle check automatically on server start
    }

    private void loadConfig() {
        idleMinutes = getConfig().getInt("idle-minutes", 10);
        numberOfPlayers = getConfig().getInt("number-of-players", 0);
        broadcastRestart = getConfig().getBoolean("broadcast-restart", true);
        restartSound = Sound.valueOf(getConfig().getString("restart-sound", "ENTITY_PLAYER_LEVELUP"));
        joinDelayMinutes = getConfig().getInt("join-delay-minutes", 5);
        idlePrefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("idle-prefix", "[&8[&6IR🔙&8]&r]"));
    }

    public void startIdleCheck() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().size() <= numberOfPlayers) {
                    isRestarting = true;
                    restartTime = System.currentTimeMillis();
                    if (broadcastRestart) {
                        broadcastMessageWithSound(idlePrefix + " Server will restart in " + idleMinutes + " minutes due to inactivity.");
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (isRestarting) {
                                Bukkit.shutdown();
                            }
                        }
                    }.runTaskLater(IdleRestartMain.this, idleMinutes * 60 * 20L);
                }
            }
        }.runTaskTimer(this, 0L, 20L * 60L);
    }

    public void forceRestart(int minutes) {
        isRestarting = true;
        restartTime = System.currentTimeMillis();
        if (broadcastRestart) {
            broadcastMessageWithSound(idlePrefix + " Server will restart in " + minutes + " minutes due to forced restart.");
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isRestarting) {
                    Bukkit.shutdown();
                }
            }
        }.runTaskLater(this, minutes * 60 * 20L);
    }

    private void broadcastMessageWithSound(String message) {
        Bukkit.broadcastMessage(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), restartSound, 1.0f, 1.0f);
        }
    }

    public void cancelRestart() {
        isRestarting = false;
    }

    public int getIdleMinutes() {
        return idleMinutes;
    }

    public void setIdleMinutes(int idleMinutes) {
        this.idleMinutes = idleMinutes;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public boolean isRestarting() {
        return isRestarting;
    }

    public long getRestartTime() {
        return restartTime;
    }

    public int getJoinDelayMinutes() {
        return joinDelayMinutes;
    }

    public String getIdlePrefix() {
        return idlePrefix;
    }
}
