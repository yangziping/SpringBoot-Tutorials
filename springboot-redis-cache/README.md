# Redis

## RedisTemplate 

`RedisTemplate`是使用得最多的类，所以它也是Spring 操作Redis的重点内容。`RedisTemplate`是一个强大的类，首先它会自动从`RedisConnectionFactory`工厂中获取连接，然后执行对应的Redis命令，在最后还会关闭Redis的连接。这些在`RedisTemplate`中都被封装了，所以并不需要开发者关注Redis连接的闭合问题。

Spring关于Redis的序列化器有如下几种：

- OxmSerializer

  以xml格式存储，解析起来也比较复杂，效率也比较低。

- GenericToStringSerializer

  需要调用者给传一个对象到字符串互转的Converter(相当于转换为字符串的操作交给转换器去做)，使用起来其比较麻烦，还不如直接用字符串呢。不太推荐使用。

- StringRedisSerializer

  是StringRedisTemplate默认的序列化方式，key和value都会采用此方式进行序列化，是被推荐使用的，对开发者友好，轻量级，效率也比较高。

- JdkSerializationRedisSerializer

  默认的序列化方式。

  使用JDK自带的序列化方式，有明显的缺点：首先它要求存储的对象都必须实现java.io.Serializable接口，比较笨重
  其次，他存储的为二进制数据，这对开发者是不友好的。再次，因为他存储的为二进制。但是有时候，我们的Redis会在一个项目的多个project中共用，这样如果同一个可以缓存的对象在不同的project中要使用两个不同的key来分别缓存，既麻烦，又浪费。

  使用JDK提供的序列化功能。 优点是反序列化时不需要提供(传入)类型信息(class)，但缺点是需要实现Serializable接口，还有序列化后的结果非常庞大，是JSON格式的5倍左右，这样就会消耗redis服务器的大量内存。

- Jackson2JsonRedisSerializer

  把一个对象以Json的形式存储，效率高且对调用者友好。优点是速度快，序列化后的字符串短小精悍，不需要实现Serializable接口。但缺点也非常致命，那就是此类的构造函数中有一个类型参数，必须提供要序列化对象的类型信息(class)。 通过查看源代码，发现其在反序列化过程中用到了类型信息(必须根据此类型信息完成反序列化)。

- GenericJackson2JsonRedisSerializer

  和Jackson2JsonRedisSerializer方式相比，这种序列化方式不用自己手动指定对象的Class。所以其实我们就可以使用一个全局通用的序列化方式了。使用起来和JdkSerializationRedisSerializer基本一样。

对于序列化器，Spring提供了`RedisSerializer`接口，它有两个方法。这两个方法，一个是`serialize`，它能把那些可以序列化的对象转换为二进制字符串；另一个是`deserialize`，它能够通过反序列化把二进制字符串转换为Java对象。其中`JdkSerializationRedisSerializer`是`RedisTemplate`默认的序列化器。

RedisTemplate中的序列化器属性如下：

- defaultSerializer：默认序列化器，如果没有设置，则使用JdkSerializationRedisSerializer
- keySerializer：Redis键序列化器，如果没有设置，则使用默认序列化器
- valueSerializer：Redis值序列化器，如果没有设置，则使用默认序列化器
- hashKeySerializer：Redis 散列结构field序列化器，如果没有设置，则使用默认序列化器
- hashValueSerializer：Redis散列结构value序列化器，如果没有设置，则使用默认序列化器
- stringSerializer：字符串序列化器，RedisTemplate自动赋值为StringRedisSerializer对象

由于默认`JdkSerializationRedisSerializer`序列化结果不是我们想要的。因此我们需要修改`RedisTemplate`的配置。

```java
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
    RedisSerializer<String> stringRedisSerializer = redisTemplate.getStringSerializer();
    // 设置key的序列化采用StringRedisSerializer
    redisTemplate.setKeySerializer(stringRedisSerializer);
    redisTemplate.setHashKeySerializer(stringRedisSerializer);
    redisTemplate.setHashValueSerializer(stringRedisSerializer);
    return redisTemplate;
}
```



Redis数据类型操作接口如下：

```java
//获取地理位置操作接口
redisTemplate.opsForGeo();
//获取散列操作接口
redisTemplate.opsForHash();
//获取基数操作接口
redisTemplate.opsForHyperLogLog();
//获取列表操作接口
redisTemplate.opsForList();
//获取集合操作接口
redisTemplate.opsForSet();
//获取字符串操作接口
redisTemplate.opsForValue();
//获取有序集合操作接口
redisTemplate.opsForZSet();
```

有时我们可能需要对某一个键值对做连续的操作。例如，有时需要连续操作一个散列数据类型或者列表多次，这时Spring也提供了对应的BoundXXXOperations接口，获取绑定键的操作类如下所示：

```java
//获取地理位置绑定键操作接口
redisTemplate.boundGeoOps("geo");
//获取散列绑定键操作接口
redisTemplate.boundHashOps("hash");
//获取列表（链表）绑定键操作接口
redisTemplate.boundListOps("list");
//获取集合绑定键操作接口
redisTemplate.boundSetOps("set");
//获取字符串绑定键操作接口
redisTemplate.boundValueOps("string");
//获取有序集合绑定键操作接口
redisTemplate.boundzSetOps("zset");
```



## Spring Boot使用Redis

配置文件：[application.yml](https://github.com/FeiChaoyu/SpringBoot-Tutorials/blob/master/springboot-redis-cache/src/main/resources/application.yml)

这里我们配置了连接池和服务器的属性，用以连接Redis服务器，这样Spring Boot的自动装配机制就会读取这些配置来生成有关Redis的操作对象，这里它会自动生成RedisConnectionFactory、RedisTemplate、StringRedisTemplate 等常用的Redis对象。

修改默认的序列化器：[RedisConfig](https://github.com/FeiChaoyu/SpringBoot-Tutorials/blob/master/springboot-redis-cache/src/main/java/com/feichaoyu/redis/config/RedisConfig.java)

我们上面在`RedisConfig`中配置的`RedisTemplate`只有使用它自身注入之后的操作，才可以在数据展示时看到json格式的对象，但是用缓存注解是不生效的。需要再配置`RedisCacheConfiguration`才能生效。



## 实现Redis两级缓存 

此部分参考书籍**《Spring Boot 2精髓 从构建小系统到架构分布式大系统》**

Spring Boot自带的Redis缓存非常容易使用，但由于通过网络访问了Redis，效率还是比传统的跟应用部署在一起的一级缓存略慢。因此可以考虑扩展`RedisCacheManager`和`RedisCache`，在访问Redis之前，先访问一个ConcurrentHashMap实现的简单一级缓存，如果有缓存项，则返回给应用，如果没有，再从Redis中取得，并将缓存对象放到一级缓存中。

当缓存项发生变化的时候，注解`@CachePut`和`@CacheEvict`会触发`RedisCache`的`put(Object Key, Object Value)`和`evict(Object Key)`操作，两级缓存需要同时更新ConcurrentHashMap和Redis缓存，而且需要通过Redis的Pub发出通知消息，其他Spring Boot应用通过Sub来接收消息，同步更新Spring Boot应用自身的一级缓存。

### 实现自己的缓存管理器

首先，创建一个新的缓存管理器，命名为`TwoLevelCacheManager`，继承了Spring Boot的`RedisCacheManager`，重载`decorateCache`方法。返回的是新创建的`RedisAndLocalCache`缓存实现。

[TwoLevelCacheManager](https://github.com/FeiChaoyu/SpringBoot-Tutorials/blob/master/springboot-redis-cache/src/main/java/com/feichaoyu/redis/config/TwoLevelCacheManager.java)

在Spring Cache中，在缓存管理器中创建好每个缓存后，都会调用`decorateCache`方法，这样缓存管理器子类就有机会实现自己的扩展。在这段代码中，返回了自定义的`RedisAndLocalCache`实现。`publishMessage`方法提供给Cache，用于在缓存更新时使用Redis的消息机制通知其他分布式节点的一级缓存。`receiver`方法对应于`publishMessage`方法，当收到消息后，会清空一级缓存。

### 创建RedisAndLocalCache

`RedisAndLocalCache`是系统的核心，它实现了`Cache`接口，需要实现如下操作：

- `get`：通过key取出对应的缓存项，在调用父类`RedisCache`之前，会先检测本地缓存是否存在，存在则不需要调用父类的get操作；如果不存在，则调用父类的get操作后，将Redis返回的`ValueWrapper`放到本地缓存中待下次使用。
- `put`：调用父类put操作更新Redis缓存，同时广播消息，缓存改变。
- `evict`：同put操作一样，调用父类处理，清空对应的缓存，同时广播消息。
- `putlfAbsent`：同put操作一样，调用父类实现，同时广播消息。

[RedisAndLocalCache](https://github.com/FeiChaoyu/SpringBoot-Tutorials/blob/master/springboot-redis-cache/src/main/java/com/feichaoyu/redis/config/RedisAndLocalCache.java)

变量`local`代表了一个简单的缓存实现，使用了`ConcurrentHashMap`，其`get`方法有如下逻辑实现：

- 通过key从本地取出`ValueWrapper`；
- 如果`ValueWrapper`存在，则直接返回；
- 如果`ValueWrapper`不存在，则调用父类`RedisCache`取得缓存项；
- 如果缓存项为空，则说明暂时无此项，直接返回空，等待`@Cacheable`调用业务方法获取缓存项。

`put`方法的实现逻辑如下：

- 先调用`redisCache`，更新二级缓存；
- 调用`notifyOthers`方法，通知其他节点缓存更新；
- 其他节点(包括本节点)的`TwoLevelCacheManager`收到消息后，会调用`receiver`方法从而实现一级缓存。

为了简单起见，一级缓存的同步更新仅仅是清空一级缓存，并非采用同步更新缓存项。一级缓存将在下一次`get`方法调用时再次从Redis中加载最新数据。

一级缓存仅仅简单使用了Map实现，并未实现缓存的多种策略。因此，如果你的一级缓存需要各种缓存策略，还需要用一些第三方库或者自行实现，但大部分情况下`TwoLevelCacheManager`都足够使用。

### 缓存同步

当缓存发生改变的时候，需要通知分布式系统的`TwoLevelCacheManager` 清空一级缓存。这里使用Redis发布/订阅模式实现消息通知。

构造一个`TwoLevelCacheManager`较为复杂，这是因为构造`RedisCacheManager`复杂导致的，构造`RedisCacheManager` 需要如下两个参数：

- `RedisCacheWriter`，一个实现Redis操作的接口，Spring Boot 提供了NoLock和Lock两种实现，在缓存写操作的时候，前者有较高性能，而后者实现了Redis锁。
- `RedisCacheConfiguration`，用于设置缓存特性，比如缓存的TTL(存活时间)、缓存Key的前缀等，默认情况下TTL为0，不使用前缀。你可以为缓存管理器设置默认的配置，也可以为每一个缓存设置一个配置。最为重要的配置是`SerializationPair`，用于Java和Redis的序列化和反序列化操作。

以上代码实现了一二级缓存，行数不到200行代码。相对于自带的RedisCache来说，缓存效率更高。相对于专业的一二级缓存服务器来说，如Ehcache+Terracotta组合，更加轻量级。

## 测试

启动工程，浏览器中输入`http://localhost:8080/user/getUser?id=1`，第一次会把信息保存到Redis服务器中，然后第二次会从Redis中取出，同时保存到本地缓存中，以后每次都是从本地缓存中获取。

结果输出：

>Hibernate: select user0_.id as id1_0_0_, user0_.address as address2_0_0_, user0_.age as age3_0_0_, user0_.name as name4_0_0_ from user user0_ where user0_.id=?
>
>从Redis缓存获取
>
>从本地缓存获取
>
>从本地缓存获取

