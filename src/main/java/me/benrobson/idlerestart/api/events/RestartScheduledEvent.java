package me.benrobson.idlerestart.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RestartScheduledEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final int minutes;
    private final String reason;

    public RestartScheduledEvent(int minutes, String reason) {
        this.minutes = minutes;
        this.reason = reason;
    }

    public int getMinutes() {
        return minutes;
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
