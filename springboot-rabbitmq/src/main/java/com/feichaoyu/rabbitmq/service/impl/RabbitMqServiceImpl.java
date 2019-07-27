package com.feichaoyu.rabbitmq.service.impl;

import com.feichaoyu.rabbitmq.model.User;
import com.feichaoyu.rabbitmq.service.RabbitMqService;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
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
        // 实现ConfirmCallback接口和ReturnCallback接口，进行回调
        implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback, RabbitMqService {

    private String msgRouting = "rabbit.hello.world";

    @Value("${rabbitmq.queue.topic}")
    private String msgTopic = null;

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
        // 设置消息
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setConsumerTag("consumer1");
        Message message = new Message(msg.getBytes(), messageProperties);
        // 发送消息，MessagePostProcessor是发送消息后置处理器
        rabbitTemplate.convertAndSend(msgTopic, msgRouting, message, message1 -> {
            System.out.println(message1.getMessageProperties().getConsumerTag());
            return message1;
        });
    }

    // 发送用户
    @Override
    public void sendUser(User user) {
        System.out.println("发送用户消息: 【" + user + "】");
        // 设置回调
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.convertAndSend(userRouting, user);
    }

    // 回调发送成功方法
    @Override
    public void confirm(CorrelationData correlationData,
                        boolean ack, String cause) {
        if (ack) {
            System.out.println("消息成功消费");
        } else {
            System.out.println("消息消费失败:" + cause);
        }
    }

    // 回调发送失败方法
    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {
        System.out.println("return exchange: " + s1 + ", routingKey: " + s2 + ", replyCode: " + i + ", replyText: " + s);
    }
}
