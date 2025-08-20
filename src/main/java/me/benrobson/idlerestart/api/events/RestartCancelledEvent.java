package me.benrobson.idlerestart.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RestartCancelledEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String reason;

    public RestartCancelledEvent(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
