package com.feichaoyu.transaction.controller;

import com.feichaoyu.transaction.model.User;
import com.feichaoyu.transaction.service.UserService;
import com.feichaoyu.transaction.service.impl.UserBatchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author feichaoyu
 * @Date 2019/8/17
 */
@RestController
public class UserController {

    @Autowired
    private UserBatchServiceImpl userBatchService;

    @Autowired
    private UserService userService;

    @RequestMapping("batch")
    public String bacth() {
        User user1 = new User();
        user1.setName("user1");
        user1.setAge(22);

        User user2 = new User();
        user2.setName("user1");
        user2.setAge(22);

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

//        userBatchService.batchInsert(users);
        userService.insertUsers(users);

        return "success";
    }
}
