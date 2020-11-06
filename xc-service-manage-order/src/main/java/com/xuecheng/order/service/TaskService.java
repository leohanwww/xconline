package com.xuecheng.order.service;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    XcTaskRepository xcTaskRepository;
    @Autowired
    XcTaskHisRepository xcTaskHisRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;

    //查询时间节点前的任务
    public List<XcTask> findXcTaskList(Date updateTime, int size){
        Pageable pageable = PageRequest.of(0,size);
        Page<XcTask> all = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        List<XcTask> result = all.getResult();
        return result;
    }

    //发布消息到交换机
    public void publish(XcTask xcTask,String ex,String routingKey){
        Optional<XcTask> optional = xcTaskRepository.findById(xcTask.getId());
        if (optional.isPresent()){
            rabbitTemplate.convertAndSend(ex,routingKey,xcTask);
            //更新updatetime
            XcTask task = optional.get();
            task.setUpdateTime(new Date());
            xcTaskRepository.save(task);
        }
    }

    //获取任务
    @Transactional
    public int  getTask(String id,int version){
        int count = xcTaskRepository.updateTaskVersion(id, version);
        return count;

    }

    //结束任务
    @Transactional
    public void finishTask(String taskId){
        if (StringUtils.isNotEmpty(taskId)){
            Optional<XcTask> optional = xcTaskRepository.findById(taskId);
            if (optional.isPresent()){
                XcTask xcTask = optional.get();
                //2、添加xc_task_his
                XcTaskHis xcTaskHis = new XcTaskHis();
                BeanUtils.copyProperties(xcTask,xcTaskHis);
                xcTaskHisRepository.save(xcTaskHis);
                //1、删除xc_task
                xcTaskRepository.delete(xcTask);
            }

        }

    }
}
