package com.feichaoyu.spider.repository;

import com.feichaoyu.spider.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Author feichaoyu
 * @Date 2019/9/16
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
}
