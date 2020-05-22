package com.feichaoyu.redis.service.impl;

import com.feichaoyu.redis.model.User;
import com.feichaoyu.redis.repository.UserRepository;
import com.feichaoyu.redis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @Author feichaoyu
 * @Date 2019/7/26
 */
@Service
@CacheConfig(cacheNames = "cacheName")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 插入用户
     *
     * @param user
     * @return
     */
    @Override
    @CachePut(key = "'redis_user_'+#result.id")
    public User insertUser(User user) {
        userRepository.save(user);
        return user;
    }

    /**
     * 获取id，取参数id缓存用户
     *
     * @param id
     * @return
     */
    @Override
    @Cacheable(key = "'redis_user_'+#id")
    public User getUser(Long id) {
        Optional<User> optional = userRepository.findById(id);
        return optional.orElse(null);
    }

    /**
     * 更新数据后，充值缓存，使用condition配置项使得结果返回为null，不缓存
     *
     * @param user
     * @return
     */
    @Override
    @CachePut(condition = "#result != 'null'", key = "'redis_user_'+#result.id")
    public User updateUser(User user) {
        // 此处自调用getUser方法，该方法缓存注解失效，所以这里还会执行SQL，将查询到数据库最新数据
        if (this.getUser(user.getId()) == null) {
            return null;
        }
        userRepository.save(user);
        return user;
    }

    /**
     * 移除缓存
     *
     * @param id
     */
    @Override
    @CacheEvict(key = "'redis_user_'+#id")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
