package com.mbcoder.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;

@Service
public class ExactDateScheduler {

    private static Logger LOGGER = LoggerFactory.getLogger(ExactDateScheduler.class);

    private ThreadPoolTaskScheduler poolScheduler;

    public ExactDateScheduler() {
        poolScheduler = new ThreadPoolTaskScheduler();
        poolScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        poolScheduler.setPoolSize(1);
        poolScheduler.initialize();
        LocalDateTime now = LocalDateTime.now();
    }

    public Instant scheduleAt(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        if (time.isBefore(now)) {
            throw new IllegalArgumentException("You can not schedule this task as starting date/time is in the past");
        }

        ZoneId zone = ZoneId.of("Europe/Berlin");
        ZoneOffset zoneOffSet = zone.getRules().getOffset(time);
        Instant whenToRun = time.toInstant(zoneOffSet);
        poolScheduler.schedule(() -> realMethod(), whenToRun);
        return whenToRun;
    }

    public void scheduleDaily(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endDate)) {
            LOGGER.info("End date reached, stopping scheduling more tasks.");
            return;
        }

        // Since we haven't reached the endDate yet, we would be safe to bring startDate to NOW if it was in the past.
        // So if we try to start scheduler for 7 days but first 3 days are already in the past, we will run the remaining 4 tasks.
        LocalDateTime timeToRun = startDate.isBefore(now) ? now : startDate;
        Instant scheduledTime = scheduleAt(timeToRun);
        // Basically scheduling this method again so that it enqueues new job 1 day later, until we reach end date.
        poolScheduler.schedule(() -> scheduleDaily(startDate, endDate), scheduledTime.plus(1, ChronoUnit.DAYS).minus(5, ChronoUnit.MINUTES));
    }

    public void realMethod() {
        LOGGER.info("Running from exact Scheduler");
    }

}
