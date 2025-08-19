package me.benrobson.idlerestart.platform;

import com.velocitypowered.api.proxy.Player;
import java.util.UUID;

public class VelocityPlayerAdapter implements PlayerAdapter {
    private final Player velocityPlayer;

    public VelocityPlayerAdapter(Player velocityPlayer) {
        this.velocityPlayer = velocityPlayer;
    }

    @Override
    public UUID getUniqueId() {
        return velocityPlayer.getUniqueId();
    }

    @Override
    public String getName() {
        return velocityPlayer.getUsername();
    }

    public Player getVelocityPlayer() {
        return velocityPlayer;
    }
}
