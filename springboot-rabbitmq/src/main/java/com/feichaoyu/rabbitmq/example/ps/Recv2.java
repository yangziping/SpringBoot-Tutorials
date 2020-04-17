package com.feichaoyu.rabbitmq.example.ps;

import com.feichaoyu.rabbitmq.example.util.ConnectionUtils;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author feichaoyu
 * @Date 2019/7/23
 */
public class Recv2 {

    private static final String QUEUE_NAME = "exchange_fanout_queue_2";

    private static final String EXCHANGE_NAME = "exchange_fanout";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到Exchange
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

        // 消费者在接收到队列里的消息但没有返回确认结果之前,它不会将新的消息分发给它
        channel.basicQos(1);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("[2] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println("[2] done");
                    // 手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 不自动应答
        boolean autoAck = false;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}
