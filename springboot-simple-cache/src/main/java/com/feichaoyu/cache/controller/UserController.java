package com.feichaoyu.cache.controller;

import com.feichaoyu.cache.model.User;
import com.feichaoyu.cache.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author feichaoyu
 * @Date 2019/7/26
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/put")
    public User put(User user) {
        return userService.save(user);

    }

    @RequestMapping("/able")
    public User cacheable(User user) {
        return userService.findOne(user);

    }

    @RequestMapping("/evit")
    public String evit(Long id) {
        userService.remove(id);
        return "ok";

    }
}
