package com.feichaoyu.springbatch.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Author feichaoyu
 * @Date 2019/7/31
 */
@RestController
public class BatchController {

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;

    @GetMapping("/test")
    public void test() {
        try {
            // 第二个参数jobParameters 必须加上时间戳，否则Spring Batch框架job任务只会执行一次
            jobLauncher.run(job, new JobParametersBuilder().addDate("date", new Date()).toJobParameters());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
