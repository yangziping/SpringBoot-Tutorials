package com.feichaoyu.security.core.validate.code;

import com.feichaoyu.security.core.properties.SecurityProperties;
import com.feichaoyu.security.core.validate.code.image.ImageCodeGenerator;
import com.feichaoyu.security.core.validate.code.sms.DefaultSmsCodeSender;
import com.feichaoyu.security.core.validate.code.sms.SmsCodeSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author feichaoyu
 * @Date 2019/8/7
 */
@Configuration
public class ValidateCodeBeanConfig {

    @Autowired
    private SecurityProperties securityProperties;

    @Bean
    @ConditionalOnMissingBean(name = "imageValidateCodeGenerator")
    public ValidateCodeGenerator imageValidateCodeGenerator() {
        ImageCodeGenerator codeGenerator = new ImageCodeGenerator();
        codeGenerator.setSecurityProperties(securityProperties);
        return codeGenerator;
    }

    @Bean
    @ConditionalOnMissingBean(name = "smsCodeSender")
    public SmsCodeSender smsCodeSender() {
        return new DefaultSmsCodeSender();
    }

}
