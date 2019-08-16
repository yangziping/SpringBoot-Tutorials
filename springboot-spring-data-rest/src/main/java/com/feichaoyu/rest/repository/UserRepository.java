package com.feichaoyu.rest.repository;

import com.feichaoyu.rest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

/**
 * @Author feichaoyu
 * @Date 2019/8/16
 */
@CrossOrigin
@RepositoryRestResource(path = "user", collectionResourceRel = "user", itemResourceRel = "user")
public interface UserRepository extends JpaRepository<User, Long> {

    @RestResource(path = "address", rel = "address")
    List<User> findByAddress(String address);

    @Override
    @RestResource(exported = false)
    void deleteById(Long id);
}
