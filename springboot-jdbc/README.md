## 数据库连接方式

### 方式一

```java
try {
    // 1.提供java.sql.Driver接口实现类的对象
    Driver driver = new com.mysql.cj.jdbc.Driver();
    
    // 2.提供url，指明具体操作的数据
    String url = "jdbc:mysql://localhost:3306/study?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    
    // 3.提供Properties的对象，指明用户名和密码
    Properties properties = new Properties();
    properties.setProperty("user", "xxx");
    properties.setProperty("password", "xxx");
    
    // 4.调用driver的connect()，获取连接
    Connection connect = driver.connect(url, properties);
    System.out.println(connect);
} catch (SQLException e) {
    e.printStackTrace();
}
```

> 上述代码中显式出现了第三方数据库的 API

### 方式二

```java
try {
    // 1.实例化Driver
    String className = "com.mysql.cj.jdbc.Driver";
    Class clazz = Class.forName(className);
    Driver driver = (Driver) clazz.newInstance();

    // 2.提供url，指明具体操作的数据
    String url = "jdbc:mysql://localhost:3306/study?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";

    // 3.提供Properties的对象，指明用户名和密码
    Properties properties = new Properties();
    properties.setProperty("user", "xxx");
    properties.setProperty("password", "xxx");

    // 4.调用driver的connect()，获取连接
    Connection connect = driver.connect(url, properties);
    System.out.println(connect);
} catch (Exception e) {
    e.printStackTrace();
}
```

> 说明：相较于方式一，这里使用反射实例化 Driver，不在代码中体现第三方数据库的 API。体现了面向接口编程思想。

### 方式三

```java
try {
    // 1.数据库连接的4个基本要素：
    String url = "jdbc:mysql://localhost:3306/study?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    String user = "xxx";
    String password = "xxx";
    String driverName = "com.mysql.cj.jdbc.Driver";

    // 2.实例化Driver
    Class clazz = Class.forName(driverName);
    Driver driver = (Driver) clazz.newInstance();

    // 3.注册驱动
    DriverManager.registerDriver(driver);

    // 4.获取连接
    Connection conn = DriverManager.getConnection(url, user, password);
    System.out.println(conn);
} catch (Exception e) {
    e.printStackTrace();
}
```

> 说明：使用 DriverManager 实现数据库的连接。体会获取连接必要的4个基本要素。

### 方式四

```java
try {
    // 1.数据库连接的4个基本要素：
    String url = "jdbc:mysql://localhost:3306/study?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    String user = "root";
    String password = "fcy123";
    String driverName = "com.mysql.cj.jdbc.Driver";

    //2.加载驱动 （①实例化Driver ②注册驱动）
    Class.forName(driverName);

    //3.获取连接
    Connection conn = DriverManager.getConnection(url, user, password);
    System.out.println(conn);
} catch (Exception e) {
    e.printStackTrace();
}
```

>说明：由于在`com.mysql.cj.jdbc.Driver`中存在静态代码块，
>
>```java
> static {
>        try {
>            DriverManager.registerDriver(new Driver());
>        } catch (SQLException var1) {
>            throw new RuntimeException("Can't register driver!");
>        }
>    }
>```
>
>在加载驱动时，会执行这个静态代码块，也就是执行了`DriverManager.registerDriver(new Driver());`

### 方式五

```java
//1.加载配置文件
InputStream is = JdbcApplicationTests.class.getClassLoader().getResourceAsStream("jdbc.properties");
Properties pros = new Properties();
pros.load(is);

//2.读取配置信息
String user = pros.getProperty("user");
String password = pros.getProperty("password");
String url = pros.getProperty("url");
String driverClass = pros.getProperty("driverClass");

//3.加载驱动
Class.forName(driverClass);

//4.获取连接
Connection conn = DriverManager.getConnection(url, user, password);
System.out.println(conn);
```

配置文件`jdbc.properties`

```properties
user=xxx
password=xxx
url=jdbc:mysql://localhost:3306/study?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
driverClass=com.mysql.cj.jdbc.Driver
```



>说明：使用配置文件的方式保存配置信息，在代码中加载配置文件
>
>**使用配置文件的好处：**
>
>1. 实现了代码和数据的分离，如果需要修改配置信息，直接在配置文件中修改，不需要深入代码
>2. 如果修改了配置信息，省去重新编译的过程。

