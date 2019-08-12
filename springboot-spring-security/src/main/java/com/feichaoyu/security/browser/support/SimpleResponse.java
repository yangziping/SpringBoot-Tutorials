package com.feichaoyu.security.browser.support;

/**
 * @Author feichaoyu
 * @Date 2019/8/6
 */
public class SimpleResponse {
    public SimpleResponse(Object content) {
        this.content = content;
    }

    private Object content;

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
