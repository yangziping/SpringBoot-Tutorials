package com.feichaoyu.rabbitmq.receiver;

import com.feichaoyu.rabbitmq.model.User;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author feichaoyu
 * @Date 2019/7/23
 */
@Component
public class RabbitMessageReceiver {

    /**
     * 定义监听字符串队列名称
     *
     * @param message
     */
    @RabbitListener(queues = {"${rabbitmq.queue.msg}"})
    public void receiveMsg(Message message) {
        System.out.println("收到消息: 【" + new String(message.getBody()) + "】");
    }

    /**
     * 定义监听用户队列名称
     *
     * @param user
     */
    @RabbitListener(queues = {"${rabbitmq.queue.user}"})
    public void receiveUser(User user) {
        System.out.println("收到用户信息【" + user.getUserName() + "】");
    }
}
