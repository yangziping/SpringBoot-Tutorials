package com.feichaoyu.rabbitmq.offical_example.tx;

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

    private static final String QUEUE_NAME = "simple_queue_tx";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        String msg = "hello";

        try {
            channel.txSelect();
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());

            int a = 1 / 0;

            channel.txCommit();
        } catch (Exception e) {
            channel.txRollback();
            System.out.println("rollback");
        }

        channel.close();
        connection.close();
    }
}
