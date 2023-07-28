# Java Dynamic Scheduling Tutorial

SpringBoot has @Scheduled annotation for scheduling tasks. But it only works with fixed rates.
In this tutorial I tried to show how to create our own scheduler for dynamic scheduling.

# Breakdown of Services

## AnnotationScheduler
This service shows how to use Spring's own scheduler with a fixed rate or cron expression.

## DynamicScheduler
This service shows how to create our own scheduler and make it work with dynamic rates. In this service we can see how to use a value from database as our next execution time. We can also see using different approaches within the same scheduler such as using fixed values, dynamic values, cron expression etc.

## CancellableScheduler
Same as dynamic scheduler but on top of that we can now cancel and re-activate the scheduler on demand.

## ExactDateScheduler
In this service we see how to create a scheduler to schedule a job to be executed at an exact date instead of giving a rate.

## ExternalScheduler
This is advanced version of cancellable scheduler and it is more like a production ready version. With this we can now easily add or remove jobs from other classes. As an example, SchedulingController shows how adding and removing job works from outside.

- - -

### Disclaimer
All the above classes are to demonstrate different possibilities of custom schedulers. You can combine those functionalities in your own scheduler according to your needs. This code is written only for demonstration purposes and is not ready for production, use with caution!
