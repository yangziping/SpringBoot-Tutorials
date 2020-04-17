package com.feichaoyu.rabbitmq.example.dlx;

import com.feichaoyu.rabbitmq.example.util.ConnectionUtils;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @Author feichaoyu
 * @Date 2019/7/22
 */
public class Recv {

    private static final String QUEUE_NAME = "test_dlx_queue";
    private static final String EXCHANGE_NAME = "test_dlx_exchange";


    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 声明正常的Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "dlx.exchange");

        // 创建正常的队列
        channel.queueDeclare(QUEUE_NAME, true, false, false, arguments);

        // 绑定正常的队列到Exchange，绑定路由键
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "dlx.#");

        // 进行死信队列的声明
        channel.exchangeDeclare("dlx.exchange", "topic");
        channel.queueDeclare("dlx.queue", true, false, false, null);
        channel.queueBind("dlx.queue", "dlx.exchange", "#");

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("recv: " + msg);
            }
        };

        // 监听队列
        channel.basicConsume(QUEUE_NAME, true, consumer);

    }
}
