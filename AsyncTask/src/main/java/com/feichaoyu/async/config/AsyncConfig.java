package com.feichaoyu.async.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author feichaoyu
 * @Date 2019/7/25
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 自定义线程池
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
        // 定义线程池
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 基本线程数
        taskExecutor.setCorePoolSize(10);
        // 最大线程数
        taskExecutor.setMaxPoolSize(20);
        // 阻塞队列容量
        taskExecutor.setQueueCapacity(30);
        // 超时时间60s
        taskExecutor.setKeepAliveSeconds(60);
        // 线程名前缀
        taskExecutor.setThreadNamePrefix("async-");
        // 线程池对拒绝任务的处理策略
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化线程池
        taskExecutor.initialize();
        return taskExecutor;
    }
}
