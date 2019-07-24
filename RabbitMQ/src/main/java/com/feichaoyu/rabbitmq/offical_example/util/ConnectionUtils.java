package com.feichaoyu.rabbitmq.offical_example.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Author feichaoyu
 * @Date 2019/7/22
 */
public class ConnectionUtils {

    /**
     * 获取mq的连接
     *
     * @return
     */
    public static Connection getConnection() throws IOException, TimeoutException {
        // 定义一个连接工厂
        ConnectionFactory factory = new ConnectionFactory();

        // 设置服务地址
        factory.setHost("127.0.0.1");

        // 设置AMQP端口：5672
        factory.setPort(5672);

        // 设置vhost
        factory.setVirtualHost("/fcy");

        // 设置用户名和密码
        factory.setUsername("fcy");
        factory.setPassword("123");

        return factory.newConnection();
    }
}
