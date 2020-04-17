package com.feichaoyu.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

/**
 * @Author feichaoyu
 * @Date 2019/7/28
 */
public class TwoLevelCacheManager extends RedisCacheManager {

    /**
     * 定义一个redis的频道，没有配置的话默认叫cache，用于pub/sub
     */
    @Value("${spring.cache.redis.topic:cache}")
    String topicName;

    private RedisTemplate redisTemplate;

    public TwoLevelCacheManager(RedisTemplate redisTemplate, RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
        this.redisTemplate = redisTemplate;
    }

    /**
     * 使用自己创建的RedisAndLocalCache代替Spring Boot自带的RedisCache
     *
     * @param cache
     * @return
     */
    @Override
    protected Cache decorateCache(Cache cache) {
        return new RedisAndLocalCache(this, (RedisCache) cache);
    }

    /**
     * 通过其他分布式节点，缓存改变
     *
     * @param cacheName
     */
    public void publishMessage(String cacheName) {
        this.redisTemplate.convertAndSend(topicName, cacheName);
    }

    /**
     * 接受一个消息清空本地缓存
     *
     * @param name
     */
    public void receiver(String name) {
        RedisAndLocalCache cache = ((RedisAndLocalCache) this.getCache(name));
        if (cache != null) {
            cache.clearLocal();
        }
    }
}
