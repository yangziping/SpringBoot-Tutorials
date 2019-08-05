package com.feichaoyu.exception.controller;

import com.feichaoyu.exception.enums.CustomizeErrorCode;
import com.feichaoyu.exception.exceptions.CustomizeException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author feichaoyu
 * @Date 2019/8/4
 */
@Controller
public class TestController {

    @RequestMapping("test")
    public void test(ModelMap modelMap) {
        System.out.println(modelMap.get("testKey"));
        int a = 1 / 0;
    }

    @RequestMapping("exception")
    public void exception() {
        throw new CustomizeException(CustomizeErrorCode.MY_ERROR);
    }

//    //局部异常处理
//    @ExceptionHandler(Exception.class)
//    @ResponseBody
//    public String exHandler(Exception e) {
//        // 判断发生异常的类型是除0异常则做出响应
//        if (e instanceof ArithmeticException) {
//            return "发生了除0异常";
//        }
//        // 未知的异常做出响应
//        return "发生了未知异常";
//    }
}
