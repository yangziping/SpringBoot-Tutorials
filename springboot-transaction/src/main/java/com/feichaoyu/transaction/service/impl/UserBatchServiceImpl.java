package com.feichaoyu.transaction.service.impl;

import com.feichaoyu.transaction.model.User;
import com.feichaoyu.transaction.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author feichaoyu
 * @Date 2019/8/17
 */
@Service
public class UserBatchServiceImpl {

    @Autowired
    private UserService userService;

    /**
     * 批量新增，沿用同一个事务
     * 这里的隔离级别优先级高于配置文件中的，spring会在使用该方法的时候更改隔离级别，使用完该方法后悔恢复设置的隔离级别
     *
     * @param users
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public void batchInsert(List<User> users) {
        for (User user : users) {
            userService.insertUser(user);
        }
    }
}
