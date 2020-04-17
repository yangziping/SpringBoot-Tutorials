package com.feichaoyu.rabbitmq.example.ps;

import com.feichaoyu.rabbitmq.example.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Author feichaoyu
 * @Date 2019/7/23
 */
public class Send {

    private static final String EXCHANGE_NAME = "exchange_fanout";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 声明Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        String msg = "hello";

        channel.basicPublish(EXCHANGE_NAME, "", null, msg.getBytes());

        channel.close();
        connection.close();
    }
}
