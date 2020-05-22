package com.feichaoyu.redis.service;

import com.feichaoyu.redis.model.User;

/**
 * @Author feichaoyu
 * @Date 2019/7/26
 */
public interface UserService {

    /**
     * 获取单个用户
     *
     * @param id
     * @return
     */
    User getUser(Long id);

    /**
     * 保存用户
     *
     * @param user
     */
    User insertUser(User user);

    /**
     * 更新用户
     *
     * @param user
     * @return
     */
    User updateUser(User user);

    /**
     * 删除单个用户
     *
     * @param id
     * @return
     */
    void deleteUser(Long id);
}
