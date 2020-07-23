package com.feichaoyu.redis.distributedlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author feichaoyu
 */
@Slf4j
@Component
public class RedisDistributedLockDemo {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private long currentTime;

    /**
     * 因为System.currentTimeMillis消耗大，所以单独开一个线程定时获取系统时钟，业务在用到的地方直接获取缓存变量即可
     */
    @PostConstruct
    public void task() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleWithFixedDelay(() -> {
            this.currentTime = System.currentTimeMillis();
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void v1() {

        long lockTimeout = 5000L;

        /*
         * 这里会出现一个问题：如果在执行完下面语句时，这台机器突然宕机，那么该锁就永远不会释放了。
         * 解决方案见v2
         */
        boolean setResult = redisTemplate.opsForValue().setIfAbsent("lock", String.valueOf(currentTime + lockTimeout));
        // 如果返回值是true，代表设置成功，获取锁
        if (setResult) {
            // 1.设置过期时间，防止死锁
            redisTemplate.expire("lock", 5, TimeUnit.SECONDS);
            // 2.业务，比如关闭订单

            // 3.删除对应key
            // TODO：需要对比value是否是自己的value，才释放。可以使用lua脚本将校验和删除放在一起执行
            redisTemplate.delete("lock");
        } else {
            log.info("没有获得分布式锁:{}", "lock");
        }
        log.info("任务结束");
    }

    /**
     * 双重防死锁
     */
    public void v2() {

        long lockTimeout = 5000L;
        boolean setResult = redisTemplate.opsForValue().setIfAbsent("lock", String.valueOf(currentTime + lockTimeout));
        // 如果返回值是true，代表设置成功，获取到锁
        if (setResult) {
            // 1.设置过期时间，防止死锁
            redisTemplate.expire("lock", 5, TimeUnit.SECONDS);
            // 2.业务，比如关闭订单

            // 3.删除对应key
            // TODO：需要对比value是否是自己的value，才释放。可以使用lua脚本将校验和删除放在一起执行
            redisTemplate.delete("lock");
        } else {
            // 未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = redisTemplate.opsForValue().get("lock");
            // 当前有人获取到锁，并且该锁内的值相比现在已经超时（此处超时不是expire设置的值），这种情况可能是对方宕机，发生了死锁
            if (lockValueStr != null && currentTime > Long.parseLong(lockValueStr)) {
                // 那么就重新写入新值，同时获取旧值
                String getSetResult = redisTemplate.opsForValue().getAndSet("lock", String.valueOf(currentTime + lockTimeout));
                // 以下两种情况能获取到锁：
                // 1.当key没有旧值时，即key不存在时，返回nil -> 获取锁
                // 2.存在旧值（也就是发生死锁），并且旧值和之前获取的lockValueStr相等（因为有可能中间有其他人获取到锁，修改了lockValueStr） -> 获取锁
                if (getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr, getSetResult))) {
                    // 获取到锁
                    // 1.设置过期时间，防止死锁
                    redisTemplate.expire("lock", 5, TimeUnit.SECONDS);
                    // 2.业务，比如关闭订单

                    // 3.删除对应key
                    // TODO：需要对比value是否是自己的value，才释放。可以使用lua脚本将校验和删除放在一起执行
                    redisTemplate.delete("lock");
                } else {
                    log.info("没有获取到分布式锁:{}", "lock");
                }
            } else {
                log.info("没有获取到分布式锁:{}", "lock");
            }
        }
        log.info("任务结束");
    }
}
