package me.benrobson.idlerestart.api;

import me.benrobson.idlerestart.IdleRestartCore;
import me.benrobson.idlerestart.IdleRestartCore.RestartType;

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
     * Schedules a server restart with the specified restart type.
     *
     * @param minutes The number of minutes until the restart.
     * @param reason  The reason for the restart.
     * @param type    The type of restart to schedule.
     */
    public void scheduleRestart(int minutes, String reason, RestartType type) {
        core.scheduleRestart(minutes, reason, type);
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

    /**
     * Gets the type of restart currently scheduled.
     *
     * @return The current restart type.
     */
    public RestartType getCurrentRestartType() {
        return core.getCurrentRestartType();
    }

    /**
     * Checks if a scheduled restart force escalation is pending.
     *
     * @return true if a scheduled restart force check is pending, false otherwise.
     */
    public boolean isScheduledRestartForcePending() {
        return core.isScheduledRestartForcePending();
    }

    /**
     * Gets the time when a scheduled restart force check will occur.
     *
     * @return The time in milliseconds since the epoch, or 0 if no force check is pending.
     */
    public long getScheduledForceCheckTimeMillis() {
        return core.getScheduledForceCheckTimeMillis();
    }
}
