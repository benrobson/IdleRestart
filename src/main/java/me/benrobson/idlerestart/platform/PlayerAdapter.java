package me.benrobson.idlerestart.platform;

import java.util.UUID;

public interface PlayerAdapter {
    UUID getUniqueId();
    String getName();
    // Add other methods if needed, e.g., for sending messages directly or location for sound
}
