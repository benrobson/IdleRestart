package me.benrobson.idlerestart.platform;

import com.velocitypowered.api.scheduler.ScheduledTask;

public class VelocitySchedulerTask implements SchedulerTask {
    private ScheduledTask velocityTask; // Can be null if task is wrapped and not yet scheduled
    private boolean cancelled = false;
    private Runnable originalTask; // Keep a reference if needed for re-scheduling or inspection

    // Constructor for tasks that are immediately scheduled by Velocity
    public VelocitySchedulerTask(ScheduledTask velocityTask) {
        this.velocityTask = velocityTask;
        this.cancelled = false; // Assume not cancelled initially
    }

    // Constructor for tasks that might be wrapped before scheduling
    public VelocitySchedulerTask(Runnable originalTask) {
        this.originalTask = originalTask;
        this.cancelled = false;
    }

    public void setActualTask(ScheduledTask velocityTask) {
        this.velocityTask = velocityTask;
    }

    public Runnable getOriginalTask() {
        return originalTask;
    }

    @Override
    public void cancel() {
        if (!cancelled) {
            cancelled = true;
            if (velocityTask != null) {
                velocityTask.cancel();
            }
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
