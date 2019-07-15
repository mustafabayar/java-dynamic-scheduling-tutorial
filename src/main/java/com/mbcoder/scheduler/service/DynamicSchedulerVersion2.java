package com.mbcoder.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

/**
 * Alternative version for DynamicScheduler
 * This one should support everything the basic dynamic scheduler does,
 * and on top of it, you can cancel and re-activate the scheduler.
 */
@Service
public class DynamicSchedulerVersion2 implements SchedulingConfigurer {

    private static Logger LOGGER = LoggerFactory.getLogger(DynamicScheduler.class);

    ScheduledTaskRegistrar scheduledTaskRegistrar;

    ScheduledFuture future;

    @Bean
    public TaskScheduler poolScheduler2() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        scheduler.setPoolSize(1);
        scheduler.initialize();
        return scheduler;
    }

    // We can have multiple tasks inside the same registrar as we can see below.
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (scheduledTaskRegistrar == null) {
            scheduledTaskRegistrar = taskRegistrar;
        }
        if (taskRegistrar.getScheduler() == null) {
            taskRegistrar.setScheduler(poolScheduler2());
        }

        future = taskRegistrar.getScheduler().schedule(() -> scheduleFixed(), t -> {
            Calendar nextExecutionTime = new GregorianCalendar();
            Date lastActualExecutionTime = t.lastActualExecutionTime();
            nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
            nextExecutionTime.add(Calendar.SECOND, 7);
            return nextExecutionTime.getTime();
        });

        // or cron way, you can also get the expression from DB or somewhere else just like we did in DynamicScheduler service.
        CronTrigger croneTrigger = new CronTrigger("0/10 * * * * ?", TimeZone.getDefault());
        future = taskRegistrar.getScheduler().schedule(() -> scheduleCron("0/10 * * * * ?"), croneTrigger);
    }

    public void scheduleFixed() {
        LOGGER.info("scheduleFixed: Next execution time of this will always be 5 seconds");
    }

    // Only reason this method gets the cron as parameter is for debug purposes.
    public void scheduleCron(String cron) {
        LOGGER.info("scheduleCron: Next execution time of this taken from cron expression -> {}", cron);
    }

    /**
     * @param mayInterruptIfRunning {@code true} if the thread executing this task
     * should be interrupted; otherwise, in-progress tasks are allowed to complete
     */
    public void cancelTasks(boolean mayInterruptIfRunning) {
        LOGGER.info("Cancelling all tasks");
        future.cancel(mayInterruptIfRunning); // set to false if you want the running task to be completed first.
    }

    public void activateScheduler() {
        LOGGER.info("Re-Activating Scheduler");
        configureTasks(scheduledTaskRegistrar);
    }

}
