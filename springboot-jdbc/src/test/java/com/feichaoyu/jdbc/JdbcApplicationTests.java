package com.feichaoyu.jdbc;

import com.mysql.cj.jdbc.Driver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JdbcApplicationTests {

    @Test
    public void test1() {
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

    }

    @Test
    public void test2() {
        try {
            // 1.实例化Driver
            String className = "com.mysql.cj.jdbc.Driver";
            Class clazz = Class.forName(className);
            Driver driver = (Driver) clazz.newInstance();

            // 2.提供url，指明具体操作的数据
            String url = "jdbc:mysql://localhost:3306/study?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";

            // 3.提供Properties的对象，指明用户名和密码
            Properties properties = new Properties();
            properties.setProperty("user", "root");
            properties.setProperty("password", "fcy123");

            // 4.调用driver的connect()，获取连接
            Connection connect = driver.connect(url, properties);
            System.out.println(connect);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test3() {
        try {
            // 1.数据库连接的4个基本要素：
            String url = "jdbc:mysql://localhost:3306/study?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
            String user = "root";
            String password = "fcy123";
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

    }

    @Test
    public void test4() {
        try {
            // 1.数据库连接的4个基本要素：
            String url = "jdbc:mysql://localhost:3306/study?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
            String user = "root";
            String password = "fcy123";
            String driverName = "com.mysql.cj.jdbc.Driver";

            //2.加载驱动 （①实例化Driver ②注册驱动）
            Class.forName(driverName);

            //Driver driver = (Driver) clazz.newInstance();
            //3.注册驱动
            //DriverManager.registerDriver(driver);
            /*
            可以注释掉上述代码的原因，是因为在mysql的Driver类中声明有：
            static {
                try {
                    DriverManager.registerDriver(new Driver());
                } catch (SQLException var1) {
                    throw new RuntimeException("Can't register driver!");
                }
            }

             */

            //3.获取连接
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test5() throws Exception {
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

    }

    @Test
    public void testInsert() {

    }

}
