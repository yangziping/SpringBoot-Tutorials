package com.feichaoyu.security.core.validate.code;

import org.springframework.security.core.AuthenticationException;

/**
 * @Author feichaoyu
 * @Date 2019/8/7
 */
public class ValidateCodeException extends AuthenticationException {

    /**
     *
     */
    private static final long serialVersionUID = -7285211528095468156L;

    public ValidateCodeException(String msg) {
        super(msg);
    }

}
