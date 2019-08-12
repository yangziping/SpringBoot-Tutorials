package com.feichaoyu.security.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author feichaoyu
 * @Date 2019/8/6
 */
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private BrowserProperties browser = new BrowserProperties();

    private ValidateCodeProperties code = new ValidateCodeProperties();

    public ValidateCodeProperties getCode() {
        return code;
    }

    public void setCode(ValidateCodeProperties code) {
        this.code = code;
    }

    public BrowserProperties getBrowser() {
        return browser;
    }

    public void setBrowser(BrowserProperties browser) {
        this.browser = browser;
    }

}
