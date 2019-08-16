# REST

REST（Representational State Transfer）是一种Web软件架构风格，它是一种风格，而不是标准，匹配或兼容这种架构风格的网络服务称为REST服务。REST服务简洁并且有层次，REST通常基于HTTP、URI和XML以及HTML这些现有的广泛流行的协议和标准。在REST中，资源是由URI来指定的，对资源的增删改查操作可以通过HTTP协议提供的GET、POST、PUT、DELETE等方法实现。使用REST可以更高效地利用缓存来提高响应速度，同时REST中的通信会话状态由客户端来维护，这可以让不同的服务器处理一系列请求中的不同请求，进而提高服务器的扩展性。

在前后端分离项目中，一个设计良好的Web软件架构必然要满足REST风格。

在Spring MVC框架中，开发者可以通过`@RestController`注解开发一个RESTful服务，不过，Spring Boot 对此提供了自动化配置方案，开发者只需要添加相关依赖就能快速构建一个RESTful服务。

# Spring Data REST

Spring Data JPA是基于Spring Data的repository之上，可以将repository自动输出为REST资源。目前Spring Data REST支持将Spring DataJPA、Spring Data MongoDB、Spring Data Neo4j、Spring Data GemFire 以及Spring Data Cassandra的 repository 自动转换成REST服务。

## Spring MVC中配置Spring Data REST

Spring Data REST的配置在`org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration`配置类中已经配置好了，我们可以通过继承此类或者直接在自己的配置类上`@Import`此配置类。

## Spring Boot中配置Spring Data REST

通过SpringBootRepositoryRestMvcConfiguration类的源码我们可以得出，Spring Boot已经为我们自动配置RepositoryRestConfiguration，所以在Spring Boot 中使用Spring Data REST只需引入`spring-boot-starter-data-rest`的依赖，无须任何配置即可使用。

在application.properties中配置以“`spring.data.rest`”为前缀的属性来配置RepositoryRestConfiguration。

## 使用

启动项目，打开PostMan测试。

查询所有：`localhost:8080/users` GET

查询指定：`localhost:8080/users/1` GET

分页查询：`localhost:8080/users?page=0&size=3` GET

按顺序分页查询：`localhost:8080/users?page=0&size=3&sort=id,desc` GET

添加：`localhost:8080/users` POST （注意这里必须是raw格式，Content-Type=application/json）

修改：`localhost:8080/users/1` PUT

删除：`localhost:8080/users/1` DELETE



### 自定义请求路径

默认情况下，请求路径都是实体类名小写加s，如果开发者想对请求路径进行重定义，通过`@RepositoryRestResource`注解即可实现。

```java
@RepositoryRestResource(path = "user", collectionResourceRel = "user", itemResourceRel = "user")
public interface UserRepository extends JpaRepository<User, Long> {
}
```

`path`属性表示将所有请求路径中的`users`都修改为`user`，`collectionResourceRel`属性表示将返回的JSON集合中User集合的key修改为`user`；`itemResourceRel`表示将返回的JSON集合中的单个User的key修改为`user`。



### 自定义查询方法

默认的查询方法支持分页查询、排序查询以及按照id查询，如果开发者想要按照某个属性查询，只需在XxxRepository中定义相关方法并暴露出去即可，代码如下：

```java
@RepositoryRestResource(path = "user", collectionResourceRel = "user", itemResourceRel = "user")
public interface UserRepository extends JpaRepository<User, Long> {

    @RestResource(path = "address", rel = "address")
    List<User> findByAddress(String address);
}
```

自定义查询只需要在XxxRepository中定义相关查询方法即可，方法定义好之后可以不添加`@RestResource`注解，默认路径就是方法名。以上面自定义的查询方法为例，若不添加@RestResource注解，则默认该方法的调用路径为`localhost:8080/user/search/findByAddress?address=xxx`。反之，若添加了@RestResource注解，那么调用路径为`localhost:8080/user/search/address?address=xxx`。

用户可以直接访问`localhost:8080/user/search`查看该实体类暴露出来了哪些查询方法，默认情况下，在查询方法展示时使用的路径是方法名，通过@RestResource注解中的`rel`属性可以对这里的路径进行重定义。



### 隐藏方法

默认情况下，凡是继承了Repository接口（或者Repository的子类）的类都会被暴露出来，即开发者可执行基本的增删改查方法。以UserRepository为例，如果开发者提供了UserRepository继承自Repository，就能执行对User的基本操作，如果开发者继承了Repository但是又不想暴露相关操作，可以在`@RepositoryRestResource`中配置`exported = false`。但是这样配置的话之前的增删查改接口都会失效，UserRepository定义的方法也会失效。若只是单纯地不想暴露某个方法，则在方法上进行配置即可，比如只想隐藏DELETE操作，那么可以写成如下形式：

```java
@Override
@RestResource(exported = false)
void deleteById(Long id);
```



### 配置CORS

CORS有两种不同的配置方式，一种是直接在方法上添加`@CrossOrigin`注解，另一种是全局配置。全局配置在这里依然适用，但是默认的RESTful工程不需要开发者自己提供Controller，因此添加在Controller的方法上的注解可以直接写在XxxRepository上，代码如下：

```java
@CrossOrigin
@RepositoryRestResource(path = "user", collectionResourceRel = "user", itemResourceRel = "user")
public interface UserRepository extends JpaRepository<User, Long> {

    @RestResource(path = "address", rel = "address")
    List<User> findByAddress(String address);

    @Override
    @RestResource(exported = false)
    void deleteById(Long id);
}
```



### 其他配置

可以在application.properties中配置如下信息：

```properties
# 每页默认记录数，默认值为20
spring.data.rest.default-page-size=10
# 分页查询页码参数名，默认值为page 
spring.data.rest.page-param-name=page
# 分页查询记录数参数名，默认值为size
spring.data.rest.limit-param-name=size
# 分页查询排序参数名，默认值为sort 
spring.data.rest.sort-param-name=sort
# base-path表示给所有请求路径都加上前缀
spring.data.rest.base-path=/api
# 添加成功时是否返回添加内容
spring.data.rest.return-body-on-create=true
# 更新成功时是否返回更新内容
spring.data.rest.return-body-on-update=true
```

当然也可以用代码形式配置，并且代码形式配置比配置文件的优先级高。

```java
public class RestConfig implements RepositoryRestConfigurer {
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.setBasePath("/api");
         // 返回主键id
        config.exposeIdsFor(User.class);
    }
}
```



最后，我有一个问题，为什么上面返回的JSON数据中包含一些"看不懂"的属性，后来上网查一下，这叫HATEOAS约束。

找了几篇文章参考阅读下，需要的可以看看。

https://www.jianshu.com/p/65b9e54dee7d

https://book.crifan.com/books/http_restful_api/website/restful_rule/hateoas.html

[理解RESTful架构-阮一峰](https://link.jianshu.com/?t=http://www.ruanyifeng.com/blog/2011/09/restful.html)

[RESTful API 设计指南-阮一峰](https://link.jianshu.com/?t=http://www.ruanyifeng.com/blog/2014/05/restful_api.html)

[我所理解的RESTful Web API](https://www.cnblogs.com/artech/p/3506553.html)

