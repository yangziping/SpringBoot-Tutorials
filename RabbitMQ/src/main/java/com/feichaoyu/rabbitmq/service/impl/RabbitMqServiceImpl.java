package com.feichaoyu.rabbitmq.service.impl;

import com.feichaoyu.rabbitmq.model.User;
import com.feichaoyu.rabbitmq.service.RabbitMqService;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @Author feichaoyu
 * @Date 2019/7/23
 */
@Service
public class RabbitMqServiceImpl
        // 实现ConfirmCallback接口，这样可以回调
        implements RabbitTemplate.ConfirmCallback, RabbitMqService {

    @Value("${rabbitmq.queue.msg}")
    private String msgRouting = null;

    @Value("${rabbitmq.queue.user}")
    private String userRouting = null;

    // 注入由Spring Boot自动配置的RabbitTemplate
    @Autowired
    private RabbitTemplate rabbitTemplate = null;

    // 发送消息
    @Override
    public void sendMsg(String msg) {
        System.out.println("发送消息: 【" + msg + "】");
        // 设置回调
        rabbitTemplate.setConfirmCallback(this);
        // 发送消息，通过msgRouting确定队列
        rabbitTemplate.convertAndSend(msgRouting, msg);
    }

    // 发送用户
    @Override
    public void sendUser(User user) {
        System.out.println("发送用户消息: 【" + user + "】");
        // 设置回调
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.convertAndSend(userRouting, user);
    }

    // 回调确认方法，在消费者得到消费时就会回调，而不是消费完才回调
    @Override
    public void confirm(CorrelationData correlationData,
                        boolean ack, String cause) {
        if (ack) {
            System.out.println("消息成功消费");
        } else {
            System.out.println("消息消费失败:" + cause);
        }
    }

}
