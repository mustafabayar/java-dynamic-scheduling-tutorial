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

import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * Alternative version for DynamicScheduler
 * This one should support everything the basic dynamic scheduler does,
 * and on top of it, you can cancel and re-activate the scheduler.
 *
 * Also nothing here is set in stone, you can change the implementation depending on your needs,
 * this is just an example about how to cancel and re-activate
 */
@Service
public class CancellableScheduler implements SchedulingConfigurer {

    private static Logger LOGGER = LoggerFactory.getLogger(CancellableScheduler.class);

    ScheduledTaskRegistrar scheduledTaskRegistrar;

    ScheduledFuture future1;
    ScheduledFuture future2;
    ScheduledFuture future3;
    Map<ScheduledFuture, Boolean> futureMap = new HashMap<>();

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

        if (future1 == null || (future1.isCancelled() && futureMap.get(future1) == true)) {
            future1 = taskRegistrar.getScheduler().schedule(() -> scheduleFixed(5), t -> {
                Calendar nextExecutionTime = new GregorianCalendar();
                Date lastActualExecutionTime = t.lastActualExecutionTime();
                nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
                nextExecutionTime.add(Calendar.SECOND, 5);
                return nextExecutionTime.getTime();
            });
        }

        if (future2 == null || (future2.isCancelled() && futureMap.get(future2) == true)) {
            future2 = taskRegistrar.getScheduler().schedule(() -> scheduleFixed(8), t -> {
                Calendar nextExecutionTime = new GregorianCalendar();
                Date lastActualExecutionTime = t.lastActualExecutionTime();
                nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
                nextExecutionTime.add(Calendar.SECOND, 8);
                return nextExecutionTime.getTime();
            });
        }

        // Or cron way, you can also get the expression from DB or somewhere else just like we did in DynamicScheduler service.
        if (future3 == null || (future3.isCancelled() && futureMap.get(future3) == true)) {
            CronTrigger croneTrigger = new CronTrigger("0/10 * * * * ?", TimeZone.getDefault());
            future3 = taskRegistrar.getScheduler().schedule(() -> scheduleCron("0/10 * * * * ?"), croneTrigger);
        }
    }

    public void scheduleFixed(int frequency) {
        LOGGER.info("scheduleFixed: Next execution time of this will always be {} seconds", frequency);
    }

    // Only reason this method gets the cron as parameter is for debug purposes.
    public void scheduleCron(String cron) {
        LOGGER.info("scheduleCron: Next execution time of this taken from cron expression -> {}", cron);
    }

    /**
     * @param mayInterruptIfRunning {@code true} if the thread executing this task
     * should be interrupted; otherwise, in-progress tasks are allowed to complete
     */
    public void cancelFuture(boolean mayInterruptIfRunning, ScheduledFuture future) {
        LOGGER.info("Cancelling a future");
        future.cancel(mayInterruptIfRunning); // set to false if you want the running task to be completed first.
        futureMap.put(future, false);
    }

    public void activateFuture(ScheduledFuture future) {
        LOGGER.info("Re-Activating a future");
        futureMap.put(future, true);
        configureTasks(scheduledTaskRegistrar);
    }

    public void cancelAll() {
        cancelFuture(true, future1);
        cancelFuture(true, future2);
        cancelFuture(true, future3);
    }

    public void activateAll() {
        activateFuture(future1);
        activateFuture(future2);
        activateFuture(future3);
    }

}
