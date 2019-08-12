package com.feichaoyu.security.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author feichaoyu
 * @Date 2019/8/12
 */
@RestController
public class TestController {

    @RequestMapping("test")
    public String test() {
        return "hello world!";
    }
}
