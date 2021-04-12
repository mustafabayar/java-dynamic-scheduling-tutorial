package com.mbcoder.scheduler.controller;

import com.mbcoder.scheduler.service.ExternalScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// This is a very basic controller just to show how scheduler can be called from outside,
// this is not a good exemplary controller and endpoints are not well designed
@RestController
public class SchedulingController {

    @Autowired
    ExternalScheduler externalScheduler;

    @PostMapping("/scheduler/add")
    @ResponseBody
    public String addJob(@RequestParam String name) {
        boolean result = externalScheduler.addJob(name);
        if (result) {
            return "Job successfully scheduled!";
        }
        return "There is already a job running for the specified name!";
    }

    @PostMapping("/scheduler/remove")
    @ResponseBody
    public String removeJob(@RequestParam String name) {
        boolean result = externalScheduler.removeJob(name);
        if (result) {
            return "Job successfully removed!";
        }
        return "There is no such job running!";
    }
}
