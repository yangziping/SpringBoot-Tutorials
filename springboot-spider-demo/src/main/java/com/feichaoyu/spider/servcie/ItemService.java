package com.feichaoyu.spider.servcie;

import com.feichaoyu.spider.model.Item;

import java.util.List;

/**
 * @Author feichaoyu
 * @Date 2019/9/16
 */
public interface ItemService {

    /**
     * 保存商品
     *
     * @param item
     */
    public void save(Item item);

    /**
     * 根据条件查询产品
     *
     * @param item
     * @return
     */
    public List<Item> findAll(Item item);
}
