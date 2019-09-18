package com.feichaoyu.spider.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feichaoyu.spider.model.Item;
import com.feichaoyu.spider.servcie.ItemService;
import com.feichaoyu.spider.util.HttpClientUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @Author feichaoyu
 * @Date 2019/9/16
 */
@Component
public class ItemTask {

    @Autowired
    private ItemService itemService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Scheduled(fixedDelay = 100 * 1000)
    public void itemTask() throws Exception {

        /**
         * 爬取不了，直接返回登录
         */
        String url = "https://search.jd.com/Search?keyword=%E6%89%8B%E6%9C%BA&enc=utf-8&qrst=1&rt=1&stop=1&vt=2&wq=%E6%89%8B%E6%9C%BA&cid2=653&cid3=655&s=1&click=0&page=";
        for (int i = 1; i < 10; i = i + 2) {
            String html = HttpClientUtils.doGetHtml(url + i);
            System.out.println(html);
            parse(html);
        }
    }

    private void parse(String html) throws Exception {
        // 解析html获取Document
        Document document = Jsoup.parse(html);
        // 获取spu信息
        Elements spuEles = document.select("div#J_goodsList > ul > li");
        for (Element spuEle : spuEles) {
            // 获取spu
            long spu = Long.parseLong(spuEle.attr("data-spu"));
            // 获取sku信息
            Elements skuEles = spuEle.select("li.ps-item");
            for (Element skuEle : skuEles) {
                // 获取sku
                long sku = Long.parseLong(skuEle.select("[data-sku]").attr("data-sku"));
                // 根据sku查询商品数据
                Item item = new Item();
                item.setSku(sku);
                List<Item> list = itemService.findAll(item);
                if (list.size() > 0) {
                    continue;
                }
                // 设置商品spu
                item.setSpu(spu);
                // 获取商品详情的url
                String itemUrl = "https://item.jd.com/" + sku + ".html";
                item.setUrl(itemUrl);
                // 获取图片
                String picUrl = "https:" + skuEle.select("img[data-sku]").first().attr("data-lazy-img");
                picUrl = picUrl.replace("/n9/", "/n1/");
                String picName = HttpClientUtils.doGetImage(picUrl);
                item.setPic(picName);
                // 获取价格
                String priceJson = HttpClientUtils.doGetHtml("https://p.3.cn/prices/mgets?skuIds=J_" + sku);
                double price = MAPPER.readTree(priceJson).get(0).get("p").asDouble();
                item.setPrice(price);
                // 获取标题
                String itemInfo = HttpClientUtils.doGetHtml(item.getUrl());
                String title = Jsoup.parse(itemInfo).select("div.sku-name").text();
                item.setTitle(title);
                item.setCreated(new Date());
                item.setUpdated(item.getCreated());
                // 保存商品到数据库
                itemService.save(item);
                System.out.println("数据抓取完成");
            }

        }
    }
}
