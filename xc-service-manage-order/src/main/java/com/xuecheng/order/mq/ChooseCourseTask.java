package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChooseCourseTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    TaskService taskService;

    //接受消息,添加选课
    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChoosecourseTask(XcTask xcTask){
        if (xcTask!=null&& StringUtils.isNotEmpty(xcTask.getId())){
            String id = xcTask.getId();
            taskService.finishTask(id);
        }
    }

 /*   //执行定时任务
    @Scheduled(cron = "0/3 * * * * *")
    public void sendChoosecourseTask() {
        //获取当前时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        //获取一分钟之前的时间
        calendar.set(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        List<XcTask> xcTaskList = taskService.findXcTaskList(time, 100);
        //调用service发布消息
        for (XcTask xcTask:xcTaskList){
            //取任务并将version+1,然后执行发布到mq
            if (taskService.getTask(xcTask.getId(),xcTask.getVersion())>0){
                String ex = xcTask.getMqExchange();
                String routingkey = xcTask.getMqRoutingkey();
                taskService.publish(xcTask,ex,routingkey);
            }

        }

    }*/

  /*  // @Scheduled(fixedRate = 3000) //上次执行开始时间后3秒执行
    // @Scheduled(fixedDelay = 5000) //上次执行完毕后5秒执行
    // @Scheduled(initialDelay=3000, fixedRate=5000) //第一次延迟3秒，以后每隔5秒执行一次
    //@Scheduled(cron = "0/3 * * * * *") //每3秒执行
    public void task1() {
        LOGGER.info("===============测试定时任务1开始===============");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOGGER.info("===============测试定时任务1结束===============");
    }

    //串行任务
    // @Scheduled(fixedRate = 3000) //上次执行开始时间后3秒执行
    // @Scheduled(fixedDelay = 5000) //上次执行完毕后5秒执行
    // @Scheduled(initialDelay=3000, fixedRate=5000) //第一次延迟3秒，以后每隔5秒执行一次
    //@Scheduled(cron = "0/3 * * * * *") //每3秒执行
    public void task2() {
        LOGGER.info("===============测试定时任务2开始===============");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOGGER.info("===============测试定时任务2结束===============");
    }*/
}
