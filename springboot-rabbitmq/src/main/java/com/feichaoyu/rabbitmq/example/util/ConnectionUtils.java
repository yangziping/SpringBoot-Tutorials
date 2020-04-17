package com.feichaoyu.rabbitmq.example.util;

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
        factory.setHost("101.200.159.42");

        // 设置AMQP端口：5672
        factory.setPort(5672);

        // 设置vhost
        factory.setVirtualHost("/");

        // 设置用户名和密码
        factory.setUsername("admin");
        factory.setPassword("123456");

        return factory.newConnection();
    }
}
