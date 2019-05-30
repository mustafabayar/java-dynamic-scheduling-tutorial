package com.mbcoder.scheduler.service;

import com.mbcoder.scheduler.model.ConfigItem;
import com.mbcoder.scheduler.repository.ConfigRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

@Service
public class DynamicScheduler implements SchedulingConfigurer {

    private static Logger LOGGER = LoggerFactory.getLogger(DynamicScheduler.class);

    @Autowired
    ConfigRepo repo;

    @PostConstruct
    public void initDatabase() {
        ConfigItem config = new ConfigItem("next_exec_time", "4");
        repo.save(config);
    }

    @Bean
    public TaskScheduler poolScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        scheduler.setPoolSize(1);
        scheduler.initialize();
        return scheduler;
    }

    // We can have multiple tasks inside the same registrar as we can see below.
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(poolScheduler());

        // Random next execution time.
        taskRegistrar.addTriggerTask(() -> scheduleDynamically(), t -> {
            Calendar nextExecutionTime = new GregorianCalendar();
            Date lastActualExecutionTime = t.lastActualExecutionTime();
            nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
            nextExecutionTime.add(Calendar.SECOND, getNextExecutionTime()); // This is where we set the next execution time.
            return nextExecutionTime.getTime();
        });

        // Fixed next execution time.
        taskRegistrar.addTriggerTask(() -> scheduleFixed(), t -> {
            Calendar nextExecutionTime = new GregorianCalendar();
            Date lastActualExecutionTime = t.lastActualExecutionTime();
            nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
            nextExecutionTime.add(Calendar.SECOND, 7); // This is where we set the next execution time.
            return nextExecutionTime.getTime();
        });

        // Next execution time is taken from DB, so if the value in DB changes, next execution time will change too.
        taskRegistrar.addTriggerTask(() -> scheduledDatabase(repo.findById("next_exec_time").get().getConfigValue()), t -> {
            Calendar nextExecutionTime = new GregorianCalendar();
            Date lastActualExecutionTime = t.lastActualExecutionTime();
            nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
            nextExecutionTime.add(Calendar.SECOND, Integer.parseInt(repo.findById("next_exec_time").get().getConfigValue()));
            return nextExecutionTime.getTime();
        });
    }

    public void scheduleDynamically() {
        LOGGER.info("scheduleDynamically: Next execution time of this changes every time between 1 and 5 seconds");
    }

    // I added this to show that one taskRegistrar can have multiple different tasks.
    // And each of those tasks can have their own next execution time.
    public void scheduleFixed() {
        LOGGER.info("scheduleFixed: Next execution time of this will always be 7 seconds");
    }

    public void scheduledDatabase(String time) {
        LOGGER.info("scheduledDatabase: Next execution time of this will be taken from DB -> {}", time);
    }

    // This is only to show that next execution time can be changed on the go with SchedulingConfigurer.
    // This can not be done via @Scheduled annotation.
    public int getNextExecutionTime() {
        return new Random().nextInt(5) + 1;
    }

}
