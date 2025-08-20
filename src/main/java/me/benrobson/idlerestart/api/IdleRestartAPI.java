package me.benrobson.idlerestart.api;

import me.benrobson.idlerestart.IdleRestartCore;

public class IdleRestartAPI {
    private final IdleRestartCore core;

    public IdleRestartAPI(IdleRestartCore core) {
        this.core = core;
    }

    /**
     * Schedules a server restart.
     *
     * @param minutes The number of minutes until the restart.
     * @param reason  The reason for the restart.
     */
    public void scheduleRestart(int minutes, String reason) {
        core.scheduleRestart(minutes, reason);
    }

    /**
     * Forces a server restart, ignoring player count.
     *
     * @param minutes The number of minutes until the restart.
     */
    public void forceRestart(int minutes) {
        core.forceRestart(minutes);
    }

    /**
     * Cancels a pending restart.
     *
     * @param reason The reason for cancelling the restart.
     */
    public void cancelRestart(String reason) {
        core.cancelRestart(reason);
    }

    /**
     * Checks if a restart is pending.
     *
     * @return true if a restart is pending, false otherwise.
     */
    public boolean isRestartPending() {
        return core.isRestartPending();
    }

    /**
     * Gets the time when the current restart was initiated.
     *
     * @return The time in milliseconds since the epoch, or 0 if no restart is pending.
     */
    public long getRestartInitiatedTimeMillis() {
        return core.getRestartInitiatedTimeMillis();
    }

    /**
     * Gets the time when the server is scheduled to restart.
     *
     * @return The time in milliseconds since the epoch, or 0 if no restart is pending.
     */
    public long getActualRestartTimeMillis() {
        return core.getActualRestartTimeMillis();
    }
}
