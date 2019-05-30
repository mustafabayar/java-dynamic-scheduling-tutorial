package com.mbcoder.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@EnableScheduling
@Service
public class AnnotationScheduler {

    private static Logger LOGGER = LoggerFactory.getLogger(AnnotationScheduler.class);

    // @Scheduled annotation always works with fixed rates and can not be changed after started.
    // Also @EnableScheduling annotation must be used to enable this annotation.
    // Using @EnableScheduling in any class is enough for enabling it for the whole project.
    @Scheduled(fixedRate = 5000, initialDelay = 10000)
    public void printFixedRate() {
        LOGGER.info("printFixedRate: Print every 5 seconds with one time initial delay of 10 seconds");
    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void printCron() {
        LOGGER.info("printCron: Print every 10 seconds with cron");
    }
}
