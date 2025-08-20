package me.benrobson.idlerestart.scheduler;

import me.benrobson.idlerestart.IdleRestartCore;
import me.benrobson.idlerestart.platform.PlatformAdapter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import me.benrobson.idlerestart.platform.SchedulerTask;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ScheduledRestartManager {
    private final IdleRestartCore core;
    private final PlatformAdapter platform;
    private final List<SchedulerTask> scheduledTasks = new ArrayList<>();

    public ScheduledRestartManager(IdleRestartCore core, PlatformAdapter platform) {
        this.core = core;
        this.platform = platform;
    }

    public void scheduleRestarts() {
        cancelTasks(); // Cancel any existing tasks before scheduling new ones
        if (!platform.isScheduledRestartEnabled()) {
            platform.info("Scheduled restarts are disabled in the configuration.");
            return;
        }

        List<String> restartTimes = platform.getScheduledRestartTimes();
        String timezone = platform.getScheduledRestartTimezone();
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timezone);
        } catch (Exception e) {
            platform.severe("Invalid timezone specified in the configuration: " + timezone + ". Defaulting to UTC.");
            zoneId = ZoneId.of("UTC");
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (String time : restartTimes) {
            try {
                java.time.LocalTime localTime = java.time.LocalTime.parse(time, formatter);
                ZonedDateTime restartTime = now.with(localTime);
                if (restartTime.isBefore(now)) {
                    restartTime = restartTime.plusDays(1);
                }

                long delay = ChronoUnit.MILLIS.between(now, restartTime);
                SchedulerTask task = platform.runTaskLater(() -> {
                    platform.info("Executing scheduled restart.");
                    core.scheduleRestart(1, "as scheduled"); // Restart in 1 minute
                }, delay / 50); // Convert to ticks
                scheduledTasks.add(task);
                platform.info("Scheduled restart for " + time + " " + timezone);
            } catch (Exception e) {
                platform.severe("Invalid time format for scheduled restart: " + time + " - " + e.getMessage());
            }
        }
    }

    public void cancelTasks() {
        for (SchedulerTask task : scheduledTasks) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        scheduledTasks.clear();
        platform.info("Cancelled all scheduled restarts.");
    }
}
