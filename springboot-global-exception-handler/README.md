参考https://www.jianshu.com/p/accec85b4039

# Spring Boot异常处理

## Spring Boot默认的异常处理机制

默认情况下，Spring Boot有以下两种默认的异常处理方式：

- 浏览器客户端请求一个不存在的页面或服务端处理发生异常时，一般情况下浏览器默认发送的请求头中Accept: text/html，所以Spring Boot默认会响应一个html文档内容，称作“Whitelabel Error Page”。
- 使用Postman等调试工具发送请求一个不存在的url或服务端处理发生异常时，Spring Boot会返回JSON格式字符串信息

原因是Spring Boot 默认提供了程序出错的结果映射路径/error。这个/error请求会在BasicErrorController中处理，其内部是通过判断请求头中的Accept的内容是否为text/html来区分请求是来自客户端浏览器(浏览器通常默认自动发送请求头内容Accept:text/html)还是客户端接口的调用，以此来决定返回页面视图还是JSON消息内容。

以下是`BasicErrorController`的源码

```java
// 浏览器请求
@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
    HttpStatus status = getStatus(request);
    Map<String, Object> model = Collections
        .unmodifiableMap(getErrorAttributes(request, isIncludeStackTrace(request, MediaType.TEXT_HTML)));
    response.setStatus(status.value());
    ModelAndView modelAndView = resolveErrorView(request, response, status, model);
    return (modelAndView != null) ? modelAndView : new ModelAndView("error", model);
}

// 客户端请求
@RequestMapping
public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
    Map<String, Object> body = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.ALL));
    HttpStatus status = getStatus(request);
    return new ResponseEntity<>(body, status);
}
```



## 自定义Whitelabel Error Page

在/resources/templates下面创建`error.html`就可以覆盖默认的Whitelabel Error Page的错误页面。

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
error错误页面
<p th:text="${timestamp}"></p>
<p th:text="${error}"></p>
<p th:text="${status}"></p>
<p th:text="${message}"></p>
<p th:text="${path}"></p>
</body>
</html>
```

当前去访问`localhots:8080`，由于没有响应转发且无`index.html`，则直接返回我们的`error.html`页面。



## 自定义错误页面

还可以根据不同的状态码返回不同的视图页面，也就是对应的404，500等页面。

这里分两种，错误页面如果是静态HTML页面，文件夹路径如下：

```
src/
 +- main/
     +- java/
     |   + <source code>
     +- resources/
         +- public/
             +- error/
             |   +- 404.html
             +- <other public assets>
```

错误页面如果是用模板引擎，比如FreeMarker，文件夹路径如下：

```
src/
 +- main/
     +- java/
     |   + <source code>
     +- resources/
         +- templates/
             +- error/
             |   +- 5xx.ftl
             +- <other templates>
```

> 注意：
>
> 1. 这时如果存在error.html，则状态码错误页面将覆盖error.html，具体状态码错误页面优先级更高。
> 2. 如果同时存在静态页面500.html和动态模板的500.html，则后者覆盖前者。即/templates/error/下的页面优先级比/resources/public/error高。
> 3. 也可以将页面命名为4xx.html或5xx.html，这样以4或5开头的状态码就会访问到对应页面。



## 后台加工错误页面

上面的方式只是对于简单的通过前端处理页面，现在需要通过后台特殊处理错误信息，但是又需要得到错误信息，此时我们可以继承`BasicErrorController`，因为原先我们使用的就是`BasicErrorController`为我们提供的错误信息，这里我们只要继承它然后对其加工即可。

```java
@Controller
public class MyErrorController extends BasicErrorController {

    public MyErrorController() {
        super(new DefaultErrorAttributes(), new ErrorProperties());
    }

    /**
     * 定义500的ModelAndView
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(produces = "text/html", value = "/500")
    public ModelAndView errorHtml500(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(getStatus(request).value());
        Map<String, Object> model = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.TEXT_HTML));
        model.put("msg", "自定义错误信息");
        return new ModelAndView("error/500", model);
    }

    /**
     * 定义500的错误JSON信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/500")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error500(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.TEXT_HTML));
        HttpStatus status = getStatus(request);
        return new ResponseEntity<>(body, status);
    }
}
```

分别对浏览器请求以及客户端请求做了500错误响应。

> 注意：
>
> BasicErrorController默认对应的@RequestMapping是`/error`，固我们方法里面对应的`@RequestMapping(produces = "text/html",value = "/500")`实际上完整的映射请求是`/error/500`。
> 

为了告诉Spring Boot完整的映射请求是/error/xxx，还需要配置内嵌Tomcat容器。

在Spring Boot2之前的配置如下：

```java
@Configuration 
public class ErrorPageConfig { 
    @Bean 
    public EmbeddedServletContainerCustomizer containerCustomizer(){ 
        return new EmbeddedServletContainerCustomizer(){ 
            @Override 
            public void customize(ConfigurableEmbeddedServletContainer container) { 
                ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND,
                    "/error/404");
           		ErrorPage errorPage400 = new ErrorPage(HttpStatus.BAD_REQUEST,
                    "/error/500");
            	ErrorPage errorPage500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR,
                    "/error/500");
                container.addErrorPages(errorPage400, errorPage404,
                    errorPage500);
            } 
        }; 
    } 
} 
```

在Spring Boot2之后的配置如下：

```java
@Configuration
public class ErrorPageConfig {
    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND,
                    "/error/404");
            ErrorPage errorPage400 = new ErrorPage(HttpStatus.BAD_REQUEST,
                    "/error/500");
            ErrorPage errorPage500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR,
                    "/error/500");
            factory.addErrorPages(errorPage400, errorPage404,
                    errorPage500);
        };
    }
}
```



## 异常处理

异常处理可以分为三种：

- 进入`@Controller`标识的方法前产生的异常，例如URL地址错误。这种异常处理需要异常处理类通过实现`ErrorController`来处理。
- 进入`@Controller`标识的方法时，但还未进行逻辑处理时产生的异常，例如传入参数数据类型错误。这种异常处理需要用`@ControllerAdvice`标识并处理，建议继承`ResponseEntityExceptionHandler`来处理，该父类包括了很多已经被`@ExceptionHandler`注解标识的方法，包括一些参数转换，请求方法不支持等类型等等。
- 进入`@Controller`标识的方法并进行逻辑处理时产生的异常，例如`NullPointerException`异常等。这种异常处理也可以用`@ControllerAdvice`来标识并进行处理，也建议继承`ResponseEntityExceptionHandler`处理， 这里我们可以用`@ExceptionHandler`自定义捕获的异常并处理。

简而言之，Spring Boot提供的`ErrorController`是一种全局性的容错机制。此外，你还可以用`@ControllerAdvice`注解和`@ExceptionHandler`注解实现对指定异常的特殊处理。

### 局部异常处理

局部异常主要用到的是`@ExceptionHandler`注解，此注解注解到类的方法上，当此注解里定义的异常抛出时，此方法会被执行。如果`@ExceptionHandler`所在的类是`@Controller`，则此方法只作用在此类。

### 全局异常处理

在Spring 3.2中，新增了`@ControllerAdvice`注解，可以用于定义`@ExceptionHandler`、`@InitBinder`、`@ModelAttribute`，并应用到所有`@RequestMapping`中。

简单的说，进入Controller层的错误才会由`@ControllerAdvice`处理，拦截器抛出的错误以及访问错误地址的情况`@ControllerAdvice`处理不了，由Spring Boot默认的异常处理机制处理，也就是我们上面讲到的错误页面处理。

###  引入枚举，抛出自定义异常

```java
public interface ICustomizeErrorCode {
    String getMessage() ;
    Integer getCode();
}
```

```java
public enum CustomizeErrorCode implements ICustomizeErrorCode {

    MY_ERROR(1000, "自定义错误"),
    ;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    private Integer code;
    private String message;

    CustomizeErrorCode(Integer code, String message) {
        this.message = message;
        this.code = code;
    }
}
```

```java
public class CustomizeException extends RuntimeException{

    private String message;
    private Integer code;

    public CustomizeException(ICustomizeErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }
}
```

测试

```java
@RequestMapping("exception")
public void exception() {
    throw new CustomizeException(CustomizeErrorCode.MY_ERROR);
}
```

结果输出：

> {
>
> ​	"msg": "自定义错误",
>
> ​	"code": 1000
>
> }