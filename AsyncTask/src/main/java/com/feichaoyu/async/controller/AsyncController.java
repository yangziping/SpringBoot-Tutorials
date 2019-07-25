package com.feichaoyu.async.controller;

import com.feichaoyu.async.service.AsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @Author feichaoyu
 * @Date 2019/7/25
 */
@RestController
@RequestMapping("/async")
public class AsyncController {

    private AsyncService asyncService;

    /**
     * 构造器注入
     *
     * @param asyncService
     * @return
     */
    @Autowired
    public AsyncController(AsyncService asyncService) {
        this.asyncService = asyncService;
    }

    @RequestMapping("task1")
    public void task1() {
        long start = System.currentTimeMillis();
        asyncService.task1();
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        // 打印异步线程的名称
        System.out.println("task1-controller: " + "【" + Thread.currentThread().getName() + "】");
    }

    @RequestMapping("futureTask")
    public void futureTask() throws ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();
        Future<String> futureTask1 = asyncService.futureTask1();
        Future<String> futureTask2 = asyncService.futureTask2();
        while (!futureTask1.isDone() || !futureTask2.isDone()) {

        }
        String s1 = futureTask1.get();
        String s2 = futureTask2.get();
        System.out.println(s1);
        System.out.println(s2);
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
