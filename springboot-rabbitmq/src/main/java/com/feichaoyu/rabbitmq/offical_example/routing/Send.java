package com.feichaoyu.rabbitmq.offical_example.routing;

import com.feichaoyu.rabbitmq.offical_example.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Author feichaoyu
 * @Date 2019/7/23
 */
public class Send {

    private static final String EXCHANGE_NAME = "exchange_direct";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 声明Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        String msg = "hello";

        String routingKey = "info";

        channel.basicPublish(EXCHANGE_NAME, routingKey, null, msg.getBytes());

        channel.close();
        connection.close();
    }
}
