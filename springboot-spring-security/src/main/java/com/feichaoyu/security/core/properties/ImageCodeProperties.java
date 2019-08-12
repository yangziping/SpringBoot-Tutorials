package com.feichaoyu.security.core.properties;

/**
 * 生成二维码默认配置
 *
 * @Author feichaoyu
 * @Date 2019/8/7
 */

public class ImageCodeProperties extends SmsCodeProperties {

    public ImageCodeProperties() {
        setLength(4);
    }

    private int width = 67;
    private int height = 23;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}
