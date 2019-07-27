package com.feichaoyu.cache.service;

import com.feichaoyu.cache.model.User;

/**
 * @Author feichaoyu
 * @Date 2019/7/26
 */
public interface UserService {

    public User save(User user);

    public void remove(Long id);

    public User findOne(User user);

}
