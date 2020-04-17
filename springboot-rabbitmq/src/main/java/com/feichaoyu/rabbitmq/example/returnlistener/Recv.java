package com.feichaoyu.rabbitmq.example.returnlistener;

import com.feichaoyu.rabbitmq.example.util.ConnectionUtils;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @Author feichaoyu
 * @Date 2019/7/22
 */
public class Recv {

    private static final String QUEUE_NAME = "test_return_listener";
    private static final String EXCHANGE_NAME = "exchange_topic";


    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 声明Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到Exchange，绑定路由键
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "return.#");

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
