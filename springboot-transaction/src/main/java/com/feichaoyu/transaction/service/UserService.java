package com.feichaoyu.transaction.service;

import com.feichaoyu.transaction.model.User;

import java.util.List;

/**
 * @Author feichaoyu
 * @Date 2019/7/26
 */
public interface UserService {
    /**
     * 获取用户信息
     *
     * @param id
     * @return
     */
    User getUser(Long id);

    /**
     * 新增用户
     *
     * @param user
     * @return
     */
    User insertUser(User user);

    /**
     * 批量新增用户
     *
     * @param users
     * @return
     */
    void insertUsers(List<User> users);
}
