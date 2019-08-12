package com.feichaoyu.security.core.validate.code;

import org.springframework.web.context.request.ServletWebRequest;

/**
 * @Author feichaoyu
 * @Date 2019/8/7
 */
public interface ValidateCodeGenerator {
    ValidateCode generate(ServletWebRequest request);
}
