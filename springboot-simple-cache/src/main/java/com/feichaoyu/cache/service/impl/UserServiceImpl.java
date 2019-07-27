package com.feichaoyu.cache.service.impl;

import com.feichaoyu.cache.model.User;
import com.feichaoyu.cache.repository.UserRepository;
import com.feichaoyu.cache.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @Author feichaoyu
 * @Date 2019/7/26
 */
@Service
@CacheConfig(cacheNames = "user")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @CachePut(key = "#user.id")
    public User save(User user) {
        User u = userRepository.save(user);
        System.out.println("为id、key为:" + u.getId() + "数据做了缓存");
        return u;
    }

    @Override
    @CacheEvict
    public void remove(Long id) {
        System.out.println("删除了id、key为" + id + "的数据缓存");
        //这里不做实际删除操作
    }

    @Override
    @Cacheable(key = "#user.id")
    public User findOne(User user) {
        Optional<User> optional = userRepository.findById(user.getId());
        if (!optional.isPresent()) {
            return null;
        } else {
            User u = optional.get();
            System.out.println("为id、key为:" + u.getId() + "数据做了缓存");
            return u;
        }
    }
}
