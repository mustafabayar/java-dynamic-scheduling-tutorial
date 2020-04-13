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
    }

    public Instant scheduleAt(LocalDate startDate, LocalTime startTime) {
        LocalDate now = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        if (startDate.isBefore(now) || (startDate.equals(now) && startTime.isBefore(currentTime))) {
            throw new IllegalArgumentException("You can not schedule this task as starting date/time is in the past");
        }

        LocalDateTime exactDateTime = startDate.atTime(startTime);
        ZoneId zone = ZoneId.of("Europe/Berlin");
        ZoneOffset zoneOffSet = zone.getRules().getOffset(exactDateTime);
        Instant whenToRun = exactDateTime.toInstant(zoneOffSet);
        poolScheduler.schedule(() -> realMethod(), whenToRun);
        return  whenToRun;
    }

    public void scheduleDaily(LocalDate startDate, LocalDate endDate, LocalTime startTime) {
        LocalDate now = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        if (now.isAfter(endDate) || (now.isEqual(endDate) && currentTime.isAfter(startTime))) {
            LOGGER.info("End date reached, stopping scheduling more tasks.");
            return;
        }

        LocalDate dateToStart = startDate;
        // Since we haven't reached the endDate yet, we would be safe to bring startDate to NOW if it was in the past.
        dateToStart = dateToStart.isBefore(now) ? now : dateToStart;

        Instant scheduledTime = scheduleAt(dateToStart, startTime);
        // Basically scheduling this method again so that it enqueues new job 1 day later, until we reach end date.
        poolScheduler.schedule(() -> scheduleDaily(startDate, endDate, startTime), scheduledTime.plus(1, ChronoUnit.DAYS).minus(5, ChronoUnit.MINUTES));
    }

    public void realMethod() {
        LOGGER.info("Running from exact Scheduler");
    }

}
