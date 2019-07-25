package com.feichaoyu.async.service;

import org.springframework.scheduling.annotation.AsyncResult;

import java.util.concurrent.Future;

/**
 * @Author feichaoyu
 * @Date 2019/7/25
 */
public interface AsyncService {
    public void task1();

    public Future<String> futureTask1();

    public Future<String> futureTask2();
}
