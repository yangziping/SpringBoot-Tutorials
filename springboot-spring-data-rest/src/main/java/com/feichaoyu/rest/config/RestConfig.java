package com.feichaoyu.rest.config;

import com.feichaoyu.rest.model.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

/**
 * @Author feichaoyu
 * @Date 2019/8/16
 */
@Configuration
public class RestConfig implements RepositoryRestConfigurer {
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.setBasePath("/api");
        // 返回主键id
        config.exposeIdsFor(User.class);
    }
}
