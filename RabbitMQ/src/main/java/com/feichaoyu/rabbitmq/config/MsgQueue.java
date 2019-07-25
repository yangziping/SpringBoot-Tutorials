package com.feichaoyu.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author feichaoyu
 * @Date 2019/7/23
 */
@Configuration
public class MsgQueue {

    // 消息队列名称
    @Value("${rabbitmq.queue.msg}")
    private String msgQueueName = null;

    // 消息队列路由键
    @Value("${rabbitmq.queue.routingKey}")
    private String msgRoutingKey = null;

    // 消息队列主题
    @Value("${rabbitmq.queue.topic}")
    private String msgTopic = null;

    // 用户队列名称
    @Value("${rabbitmq.queue.user}")
    private String userQueueName = null;

    @Bean
    public Queue createQueueMsg() {
        // 创建字符串消息队列，boolean值代表是否持久化消息
        return new Queue(msgQueueName, true);
    }

    @Bean
    public Queue createQueueUser() {
        // 创建用户消息队列，boolean值代表是否持久化消息
        return new Queue(userQueueName, true);
    }

    @Bean
    public TopicExchange exchange() {
        // 创建交换机，类型是topic，不持久化，不自动删除
        return new TopicExchange(msgTopic, false, false);
    }

    @Bean
    public Binding createBinding() {
        // 将队列以路由键rabbit.*的形式绑定到交换机
        return BindingBuilder.bind(createQueueMsg()).to(exchange()).with(msgRoutingKey);
    }
}
