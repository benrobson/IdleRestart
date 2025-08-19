package me.benrobson.idlerestart.platform;

import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitPlayerAdapter implements PlayerAdapter {
    private final Player bukkitPlayer;

    public BukkitPlayerAdapter(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
    }

    @Override
    public UUID getUniqueId() {
        return bukkitPlayer.getUniqueId();
    }

    @Override
    public String getName() {
        return bukkitPlayer.getName();
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }
}
