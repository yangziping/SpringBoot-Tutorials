package com.feichaoyu.exception.config;

import org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * 配置内嵌Tomcat，定制错误路径
 *
 * @Author feichaoyu
 * @Date 2019/8/4
 */
@Configuration
public class ErrorPageConfig {
    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND,
                    "/error/404");
            ErrorPage errorPage400 = new ErrorPage(HttpStatus.BAD_REQUEST,
                    "/error/500");
            ErrorPage errorPage500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR,
                    "/error/500");
            factory.addErrorPages(errorPage400, errorPage404,
                    errorPage500);
        };
    }
}
