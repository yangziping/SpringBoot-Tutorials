package com.feichaoyu.security.core.validate.code.sms;

/**
 * @Author feichaoyu
 * @Date 2019/8/11
 */
public interface SmsCodeSender {
    void send(String mobile, String code);
}
