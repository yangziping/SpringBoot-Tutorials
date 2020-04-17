package com.feichaoyu.rabbitmq.example.confirm;

import com.feichaoyu.rabbitmq.example.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
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

        // 方法1
        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("ack");
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("no ack");
            }
        });

        // 方法2
        if (!channel.waitForConfirms()) {
            System.out.println("failed");
        } else {
            System.out.println("ok");
        }

        channel.close();
        connection.close();
    }
}
