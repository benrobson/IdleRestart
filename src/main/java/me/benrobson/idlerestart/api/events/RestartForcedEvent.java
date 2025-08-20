package me.benrobson.idlerestart.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RestartForcedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final int minutes;

    public RestartForcedEvent(int minutes) {
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
