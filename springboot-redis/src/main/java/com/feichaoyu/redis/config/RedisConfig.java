package com.feichaoyu.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.feichaoyu.redis.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.config.JCacheConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.*;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * @Author feichaoyu
 * @Date 2019/7/27
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean(name = "redisTemplate")
    public RedisTemplate<Object, Object> initRedisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        // 设置redis默认连接工厂，由application.yml的配置指定
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 使用Jackson2JsonRedisSerialize替换默认序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // 设置value的序列化采用Jackson2JsonRedisSerializer
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        RedisSerializer<String> stringRedisSerializer = redisTemplate.getStringSerializer();
        // 设置key的序列化采用StringRedisSerializer
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {

        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        Jackson2JsonRedisSerializer<User> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(User.class);

        //ObjectMapper objectMapper = new ObjectMapper();
        //objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //对象的所有字段全部列入
        //objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        //忽略空Bean转json的错误
        //objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        //忽略在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        //objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        redisCacheConfiguration = redisCacheConfiguration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));
        return redisCacheConfiguration;
    }

    /**
     * 以下配置用于实现redis二级缓存
     */

    @Value("${spring.cache.redis.topic:cache}")
    String topicName;

    @Bean
    public TwoLevelCacheManager cacheManager(RedisTemplate redisTemplate, RedisCacheConfiguration redisCacheConfiguration) {

        //RedisCache需要一个RedisCacheWriter来实现读写Redis
        RedisCacheWriter writer = RedisCacheWriter.lockingRedisCacheWriter(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        TwoLevelCacheManager cacheManager = new TwoLevelCacheManager(redisTemplate, writer, redisCacheConfiguration);
        return cacheManager;
    }

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(topicName));
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(final TwoLevelCacheManager cacheManager) {

        return new MessageListenerAdapter((MessageListener) (message, pattern) -> {
            byte[] bs = message.getChannel();

            try {
                // 订阅一个消息，通知缓存管理器
                String type = new String(bs, "UTF-8");
                String cacheName = new String(message.getBody(), "UTF-8");
                cacheManager.receiver(cacheName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                // 不可能出错，忽略
            }

        });
    }
}
