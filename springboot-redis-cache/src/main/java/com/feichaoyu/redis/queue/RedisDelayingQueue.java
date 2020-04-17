package com.feichaoyu.redis.queue;

import com.feichaoyu.redis.util.JsonUtils;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.UUID;

/**
 * @author feichaoyu
 */
public class RedisDelayingQueue<T> {
    static class TaskItem<T> {
        public String id;
        public T msg;
    }

    private Jedis jedis;
    private String queueKey;

    public RedisDelayingQueue(Jedis jedis, String queueKey) {
        this.jedis = jedis;
        this.queueKey = queueKey;
    }

    public void delay(T msg) {
        TaskItem<T> task = new TaskItem<>();
        // 分配唯一的uuid
        task.id = UUID.randomUUID().toString();
        task.msg = msg;
        // json 序列化
        String jsonString = JsonUtils.object2Json(task);
        // 塞入延时队列，5s后再试
        jedis.zadd(queueKey, System.currentTimeMillis() + 5000, jsonString);
    }

    public void loop() {
        while (!Thread.interrupted()) {
            // 只取一条
            Set<String> values = jedis.zrangeByScore(queueKey, 0, System.currentTimeMillis(), 0, 1);
            if (values.isEmpty()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                // 歇会继续
                continue;
            }
            String jsonString = values.iterator().next();
            // 抢到了
            if (jedis.zrem(queueKey, jsonString) > 0) {
                // 反序列化
                TaskItem<T> task = JsonUtils.json2Object(jsonString, TaskItem.class);
                handleMsg(task.msg);
            }
        }
    }

    public void handleMsg(T msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis();
        RedisDelayingQueue<String> queue = new RedisDelayingQueue<>(jedis, "delaying-queue");
        Thread producer = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    queue.delay("task" + i);
                }
            }
        };
        Thread consumer = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    queue.loop();
                }
            }
        };

        producer.start();
        consumer.start();
        try {
            producer.join();
            Thread.sleep(5000);
            consumer.interrupt();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
