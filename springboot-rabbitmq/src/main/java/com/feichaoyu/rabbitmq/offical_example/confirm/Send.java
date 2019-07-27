package com.feichaoyu.rabbitmq.offical_example.confirm;

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

    private static final String QUEUE_NAME = "simple_queue_confirm";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        String msg = "hello";

        channel.confirmSelect();

        channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());

        if (!channel.waitForConfirms()) {
            System.out.println("failed");
        } else {
            System.out.println("ok");
        }

        channel.close();
        connection.close();
    }
}
