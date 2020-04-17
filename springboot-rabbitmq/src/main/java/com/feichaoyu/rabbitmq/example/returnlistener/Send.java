package com.feichaoyu.rabbitmq.example.returnlistener;

import com.feichaoyu.rabbitmq.example.util.ConnectionUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ReturnListener;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Author feichaoyu
 * @Date 2019/7/23
 */
public class Send {

    private static final String EXCHANGE_NAME = "exchange_topic";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        String msg = "hello";
        String routingKey = "return.save";
        String routingKeyError = "abc.save";

        channel.confirmSelect();

        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("------handle return------");
                System.out.println("replyCode:" + replyCode);
                System.out.println("replyText:" + replyText);
                System.out.println("exchange:" + exchange);
                System.out.println("routingKey:" + routingKey);
                System.out.println("properties:" + properties);
                System.out.println("body:" + new String(body));
            }
        });

        // 第三个参数为mandatory
        // true表示监听器会接收到路由不可达的消息，然后进行后续处理
        // false表示broker端会自动删除该消息
//        channel.basicPublish(EXCHANGE_NAME, routingKey, true, null, msg.getBytes());
        channel.basicPublish(EXCHANGE_NAME, routingKeyError, true, null, msg.getBytes());

//        channel.close();
//        connection.close();
    }
}
