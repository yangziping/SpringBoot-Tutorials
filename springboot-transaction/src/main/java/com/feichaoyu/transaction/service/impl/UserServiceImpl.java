package com.feichaoyu.transaction.service.impl;

import com.feichaoyu.transaction.model.User;
import com.feichaoyu.transaction.repository.UserRepository;
import com.feichaoyu.transaction.service.UserService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author feichaoyu
 * @Date 2019/7/26
 */
@Service
public class UserServiceImpl implements UserService, ApplicationContextAware {

    @Autowired
    private UserRepository userRepository;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public User getUser(Long id) {
        return null;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public User insertUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public void insertUsers(List<User> users) {
        // 从IoC容器中取出代理对象
        UserService userService = applicationContext.getBean(UserService.class);
        for (User user : users) {
            insertUser(user);
        }
    }

}
