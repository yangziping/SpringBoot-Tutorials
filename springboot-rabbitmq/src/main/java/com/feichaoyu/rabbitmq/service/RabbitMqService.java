package com.feichaoyu.rabbitmq.service;

import com.feichaoyu.rabbitmq.model.User;

/**
 * @Author feichaoyu
 * @Date 2019/7/23
 */
public interface RabbitMqService {

    // 发送字符消息
    public void sendMsg(String msg);

    // 发送用户消息
    public void sendUser(User user);
}
