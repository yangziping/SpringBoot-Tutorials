package com.feichaoyu.spider.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @Author feichaoyu
 * @Date 2019/9/16
 */
@Entity
@Table(name = "jd_item")
@Data
public class Item {

    /**
     * 主键id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 商品集合id
     */
    private Long spu;

    /**
     * 商品最小品类单元id
     */
    private Long sku;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品价格
     */
    private Double price;

    /**
     * 商品图片
     */
    private String pic;

    /**
     * 商品详情地址
     */
    private String url;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 更改时间
     */
    private Date updated;

}
