package me.benrobson.idlerestart.scheduler;

import me.benrobson.idlerestart.IdleRestartCore;
import me.benrobson.idlerestart.IdleRestartCore.RestartType;
import me.benrobson.idlerestart.platform.PlatformAdapter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import me.benrobson.idlerestart.platform.SchedulerTask;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

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
        ZoneId zoneId = resolveZoneId(timezone);
        platform.info("Using timezone " + zoneId.getId() + " for scheduled restarts.");

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
                long delayTicks = Math.max(1L, delay / 50L);
                SchedulerTask task = platform.runTaskLater(() -> {
                    platform.info("Executing scheduled restart.");
                    core.scheduleRestart(1, "as scheduled", RestartType.SCHEDULED); // Restart in 1 minute
                }, delayTicks); // Convert to ticks
                scheduledTasks.add(task);
                platform.info("Scheduled restart for " + time + " " + zoneId.getId());
            } catch (Exception e) {
                platform.severe("Invalid time format for scheduled restart: " + time + " - " + e.getMessage());
            }
        }
    }

    private ZoneId resolveZoneId(String timezone) {
        if (timezone == null) {
            platform.warning("No timezone specified for scheduled restarts. Defaulting to system default: " + ZoneId.systemDefault().getId());
            return ZoneId.systemDefault();
        }

        String trimmed = timezone.trim();
        if (trimmed.isEmpty()) {
            platform.warning("Empty timezone specified for scheduled restarts. Defaulting to system default: " + ZoneId.systemDefault().getId());
            return ZoneId.systemDefault();
        }

        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(trimmed);
        candidates.add(trimmed.replace(' ', '_'));
        candidates.add(trimmed.replace(' ', '/'));
        candidates.add(trimmed.replace('_', '/'));

        String lower = trimmed.toLowerCase(Locale.ROOT);
        candidates.add(lower);
        candidates.add(lower.replace(' ', '_'));
        candidates.add(lower.replace(' ', '/'));

        if (!trimmed.contains("/")) {
            candidates.add("Australia/" + trimmed.replace(' ', '_'));
            candidates.add("Australia/" + lower.replace(' ', '_'));
        }

        String upper = trimmed.toUpperCase(Locale.ROOT);
        String alias = ZoneId.SHORT_IDS.get(upper);
        if (alias != null) {
            candidates.add(alias);
        }

        for (String candidate : candidates) {
            try {
                return ZoneId.of(candidate);
            } catch (Exception ignored) {
            }
        }

        platform.severe("Invalid timezone specified in the configuration: " + timezone + ". Defaulting to UTC.");
        return ZoneId.of("UTC");
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
