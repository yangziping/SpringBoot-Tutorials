package com.feichaoyu.async.service.impl;

import com.feichaoyu.async.service.AsyncService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @Author feichaoyu
 * @Date 2019/7/25
 */
@Service
@Async
public class AsyncServiceImpl implements AsyncService {

    @Override
    public void task1() {
        // 模拟执行任务耗时
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 打印异步线程的名称
            System.out.println("task1-service: " + "【" + Thread.currentThread().getName() + "】");
        }
    }

    @Override
    public Future<String> futureTask1() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>("futureTask1 done");
    }

    @Override
    public Future<String> futureTask2() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>("futureTask2 done");
    }

}
