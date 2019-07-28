package com.feichaoyu.redis.controller;

import com.feichaoyu.redis.model.User;
import com.feichaoyu.redis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author feichaoyu
 * @Date 2019/7/27
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/getUser")
    public User getUser(Long id) {
        return userService.getUser(id);
    }

    @RequestMapping("/insertUser")
    public User insertUser(String name, String address) {
        User user = new User();
        user.setName(name);
        user.setAddress(address);
        userService.insertUser(user);
        return user;
    }

    @RequestMapping("/updateUser")
    public Map<String, Object> updateUser(User u) {
        User user = userService.updateUser(u);
        boolean flag = user != null;
        String message = flag ? "更新成功" : "更新失败";
        return resultMap(flag, message);
    }

    @RequestMapping("/deleteUser")
    public Map<String, Object> deleteUser(Long id) {
        userService.deleteUser(id);
        return resultMap(true, "删除成功");
    }

    private Map<String, Object> resultMap(boolean success, String message) {
        Map<String, Object> result = new HashMap<>(16);
        result.put("success", success);
        result.put("message", message);
        return result;
    }
}
