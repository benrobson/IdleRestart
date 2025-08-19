package me.benrobson.idlerestart.platform;

public interface SchedulerTask {
    void cancel();
    boolean isCancelled();
}
