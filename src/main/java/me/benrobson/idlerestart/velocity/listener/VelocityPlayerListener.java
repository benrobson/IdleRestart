package me.benrobson.idlerestart.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import me.benrobson.idlerestart.IdleRestartCore;

public class VelocityPlayerListener {
    private final IdleRestartCore core;

    public VelocityPlayerListener(IdleRestartCore core) {
        this.core = core;
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        // This event fires when a player has successfully logged into the proxy.
        // We can consider this as a "join" for the purpose of delaying restart.
        core.onPlayerJoin();
    }
}
