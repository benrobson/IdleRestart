package me.benrobson.idlerestart.platform;

import org.bukkit.scheduler.BukkitTask;

public class BukkitSchedulerTask implements SchedulerTask {
    private final BukkitTask bukkitTask;

    public BukkitSchedulerTask(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    @Override
    public void cancel() {
        bukkitTask.cancel();
    }

    @Override
    public boolean isCancelled() {
        return bukkitTask.isCancelled();
    }
}
