package org.kookies.mirai.commen.utils;

import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.SchedulerJobException;
import org.kookies.mirai.commen.jobs.MessageCacheCleanerJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author General_K1ng
 */
public class JobScheduler {
    /**
     * 启动定时任务，用于每天定时清理消息缓存。
     * <p>
     * 该方法通过Quartz Scheduler框架实现任务的定时执行。
     * 它创建了一个清理消息缓存的任务（Job）和一个触发器（Trigger），
     * 并将它们安排在默认的调度器中执行。任务每天凌晨2点执行一次，用于清理过期的消息缓存。
     *
     * @throws SchedulerJobException 如果调度过程中发生错误
     */
    public static void start() throws SchedulerJobException{
        try {
            // 创建JobDetail对象，定义清理消息缓存的任务
            JobDetail job = JobBuilder.newJob(MessageCacheCleanerJob.class)
                    .withIdentity("messageCacheCleanerJob", "messageCacheCleanerJobGroup")
                    .build();

            // 创建Trigger对象，定义任务的触发规则为每天凌晨2点
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("dailyTrigger", "messageCacheCleanerJobGroup")
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(2, 0))
                    .build();

            // 获取默认调度器并启动
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            // 将任务和触发器注册到调度器中
            scheduler.scheduleJob(job, trigger);
        } catch (Exception e) {
            // 如果调度过程中发生错误，抛出自定义异常
            throw new SchedulerJobException(MsgConstant.SCHEDULER_EXCEPTION);
        }
    }

}
