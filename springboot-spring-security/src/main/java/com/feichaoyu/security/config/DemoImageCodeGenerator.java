package com.feichaoyu.security.config;

import com.feichaoyu.security.core.validate.code.ValidateCodeGenerator;
import com.feichaoyu.security.core.validate.code.image.ImageCode;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * @Author feichaoyu
 * @Date 2019/8/7
 */
//@Component("imageValidateCodeGenerator")
public class DemoImageCodeGenerator implements ValidateCodeGenerator {

    @Override
    public ImageCode generate(ServletWebRequest request) {
        System.out.println("更高级的图形验证码生成代码");
        System.out.println("DemoImageCodeGenerator.generate");
        System.out.println("request = " + request);
        if (request == null) return null;
        return null;
    }

}
