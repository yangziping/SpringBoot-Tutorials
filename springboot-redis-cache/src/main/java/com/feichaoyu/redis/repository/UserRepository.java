package com.feichaoyu.redis.repository;

import com.feichaoyu.redis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Author feichaoyu
 * @Date 2019/7/26
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
