package com.feichaoyu.redis.config;

import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author feichaoyu
 * @Date 2019/7/28
 */
public class RedisAndLocalCache implements Cache {

    /**
     * 本地缓存提供
     */
    private ConcurrentHashMap<Object, Object> local = new ConcurrentHashMap<>();

    private RedisCache redisCache;

    private TwoLevelCacheManager cacheManager;

    RedisAndLocalCache(TwoLevelCacheManager cacheManager, RedisCache redisCache) {
        this.redisCache = redisCache;
        this.cacheManager = cacheManager;
    }

    @Override
    public String getName() {
        return redisCache.getName();
    }

    @Override
    public Object getNativeCache() {
        return redisCache.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper wrapper = (ValueWrapper) local.get(key);
        if (wrapper != null) {
            System.out.println("从本地缓存获取");
            return wrapper;
        } else {
            // 本地缓存不存在，则从二级缓存中取，同时放入本地缓存
            wrapper = redisCache.get(key);
            if (wrapper != null) {
                System.out.println("从Redis缓存获取");
                local.put(key, wrapper);
            }

            return wrapper;
        }

    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return redisCache.get(key, type);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return redisCache.get(key, valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        redisCache.put(key, value);
        notifyOthers();
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper v = redisCache.putIfAbsent(key, value);
        notifyOthers();
        return v;
    }

    @Override
    public void evict(Object key) {
        redisCache.evict(key);
        notifyOthers();
    }

    @Override
    public void clear() {
        redisCache.clear();
    }

    public void clearLocal() {
        this.local.clear();
    }

    /**
     * 通知其他节点缓存更新
     */
    protected void notifyOthers() {
        cacheManager.publishMessage(redisCache.getName());
    }
}
