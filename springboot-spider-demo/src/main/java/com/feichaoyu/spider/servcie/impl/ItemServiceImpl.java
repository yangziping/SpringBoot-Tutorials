package com.feichaoyu.spider.servcie.impl;

import com.feichaoyu.spider.model.Item;
import com.feichaoyu.spider.repository.ItemRepository;
import com.feichaoyu.spider.servcie.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author feichaoyu
 * @Date 2019/9/16
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public void save(Item item) {
        itemRepository.save(item);
    }

    @Override
    public List<Item> findAll(Item item) {
        // 声明查询条件
        Example<Item> example = Example.of(item);

        // 根据查询条件进行查询
        List<Item> list = itemRepository.findAll(example);
        return list;
    }
}
