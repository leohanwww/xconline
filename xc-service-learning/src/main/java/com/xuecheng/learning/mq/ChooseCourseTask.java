package com.xuecheng.learning.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.LearningService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class ChooseCourseTask {
    @Autowired
    LearningService learningService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    //接受消息,添加选课
    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE)
    public void receiveChoosecourseTask(XcTask xcTask){
        if (xcTask==null){
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        String id = xcTask.getId(); //选课id
        String requestBody = xcTask.getRequestBody();
        Map<String,String> json = JSON.parseObject(requestBody,Map.class);
        String userId = json.get("userId");
        String courseId = json.get("courseId");
        String valid = (String) json.get("valid");
        Date startTime = null;
        Date endTime = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY‐MM‐dd HH:mm:ss");
        if(json.get("startTime")!=null){
            try {
                startTime =dateFormat.parse((String) json.get("startTime"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(json.get("endTime")!=null){
            try {
                endTime =dateFormat.parse((String) json.get("endTime"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        ResponseResult addcourse = learningService.addcourse(userId, courseId, valid, startTime, endTime, xcTask);
        if (addcourse.isSuccess()){
            //添加选课成功,向mq发送完成选课的消息
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE,
                    RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY,xcTask);
        }
    }
}
