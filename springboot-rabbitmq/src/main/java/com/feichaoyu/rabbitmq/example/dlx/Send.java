package com.feichaoyu.rabbitmq.example.dlx;

import com.feichaoyu.rabbitmq.example.util.ConnectionUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @Author feichaoyu
 * @Date 2019/7/23
 */
public class Send {

    private static final String EXCHANGE_NAME = "test_dlx_exchange";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        String msg = "hello";
        String routingKey = "dlx.save";

        channel.confirmSelect();

        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .deliveryMode(2)
                .contentType(StandardCharsets.UTF_8.toString())
                // 设置消息过期时间为10s
                .expiration("10000")
                .build();
        channel.basicPublish(EXCHANGE_NAME, routingKey, properties, msg.getBytes());

    }
}
