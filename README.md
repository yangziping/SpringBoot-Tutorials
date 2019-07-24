# Message Broker和AMQP

Message Broker是一种消息验证、传输、路由的架构模式，其设计目标主要应用于下面这些场景：

- 消息路由到一个或多个目的地
- 消息转化为其他的表现方式
- 执行消息的聚集、消息的分解，并将结果发送到他们的目的地，然后重新组合相应返回给消息用户
- 调用Web服务来检索数据
- 响应事件或错误
- 使用发布-订阅模式来提供内容或基于主题的消息路由

AMQP是Advanced Message Queuing Protocol的简称，它是一个面向消息中间件的开放式标准应用层协议。AMQP定义了这些特性：

- 消息方向
- 消息队列
- 消息路由(包括：点到点和发布-订阅模式)
- 可靠性
- 安全性



# 消息队列

## 形式

- 队列：点对点
  - 消息发送者发送消息，消息代理将其放入队里中，消息接收者从队列中获取消息内容，消息读取后被移出队列
  - 消息只有唯一的发送者和接收者
- 主题：发布/订阅
  - 发送者发送消息到主题，多个接收者订阅(监听)这个主题，那么就会在消息到达时同时接收消息



























# RabbitMQ

## 持久化

### 队列持久化

```
boolean durable = true;
channel.queueDeclare("task_queue", durable, false, false, null);
```

### 消息持久化

```
channel.basicPublish("", "task_queue",
            MessageProperties.PERSISTENT_TEXT_PLAIN,
            message.getBytes());
```









## 简单队列

生产者

```java
public class Send {

    private static final String QUEUE_NAME = "simple_queue";
        public static void main(String[] args) throws IOException, TimeoutException {
            // 获取连接
            Connection connection = ConnectionUtils.getConnection();

            // 获取通道
            Channel channel = connection.createChannel();

            // 创建队列声明
            channel.queueDeclare(QUEUE_NAME,false,false,false,null);

            String msg = "hello";

            channel.basicPublish("",QUEUE_NAME,null,msg.getBytes());

        channel.close();
        connection.close();
    }
}
```



消费者

```java
public class Recv {

    private static final String QUEUE_NAME = "simple_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body,"UTF-8");
                System.out.println("recv: " + msg);
            }
        };

        // 监听队列
        channel.basicConsume(QUEUE_NAME, true, consumer);

    }
}
```



## 工作队列

### 轮询消费

生产者

```java
public class Send {

    private static final String QUEUE_NAME = "work_queue";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        for (int i = 0; i < 10; i++) {
            String msg = "hello" + i;
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
            TimeUnit.MILLISECONDS.sleep(i * 20);
        }

        channel.close();
        connection.close();
    }
}
```



消费者1

```java
public class Recv1 {

    private static final String QUEUE_NAME = "work_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("[1] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("[1] done");
                }
            }
        };

        boolean autoAck = true;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}
```



消费者2

```java
public class Recv2 {

    private static final String QUEUE_NAME = "work_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("[2] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("[2] done");
                }
            }
        };

        boolean autoAck = true;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}
```

轮询结果输出：

> 消费者1：
>
> [1] recv: hello1
> [1] done
> [1] recv: hello3
> [1] done
> [1] recv: hello5
> [1] done
> [1] recv: hello7
> [1] done
> [1] recv: hello9
> [1] done

> 消费者2：
>
> [2] recv: hello0
> [2] done
> [2] recv: hello2
> [2] done
> [2] recv: hello4
> [2] done
> [2] recv: hello6
> [2] done
> [2] recv: hello8
> [2] done

从结果看出，消费者1和消费者2都是消费5个消息，但我们想让消费者1消费的数量是消费者2的一半，因为消费者1效率低，消费者2效率高，所谓能者多劳，就是这个意思。



### 公平消费

关闭自动应答`autoAck=false`，加上`channel.basicQos(1)`，表示该消费者在接收到队列里的消息但没有返回确认结果之前,它不会将新的消息分发给它。

消费者1

```java
public class Recv1 {

    private static final String QUEUE_NAME = "work_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 消费者在接收到队列里的消息但没有返回确认结果之前,它不会将新的消息分发给它
        channel.basicQos(1);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("[1] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println("[1] done");
                    // 手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 不自动应答
        boolean autoAck = false;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}
```



消费者2

```java
public class Recv2 {

    private static final String QUEUE_NAME = "work_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 消费者在接收到队列里的消息但没有返回确认结果之前,它不会将新的消息分发给它
        channel.basicQos(1);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("[2] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println("[1] done");
                    // 手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 不自动应答
        boolean autoAck = false;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}

```

结果输出：

> 消费者1：
>
> [1] recv: hello0
> [1] done
> [1] recv: hello4
> [1] done
> [1] recv: hello7
> [1] done

> 消费者2：
>
> [2] recv: hello1
> [2] done
> [2] recv: hello2
> [2] done
> [2] recv: hello3
> [2] done
> [2] recv: hello5
> [2] done
> [2] recv: hello6
> [2] done
> [2] recv: hello8
> [2] done
> [2] recv: hello9
> [2] done

可以看出，消费者2处理消息数量是消费者1的两倍。



## 发布/订阅

以上两种模式下，一个消息只能发送给一个消费者，如何将一个消息发送给多个消费者呢？就是下面要介绍的发布/订阅模式。

![](C:/Users/mercy/Desktop/images/python-three.png)

对上图的解释：

- 一个生产者，多个消费者
- 每个消费者都有自己的队列
- 生产者没有直接把消息发送到队列，而是发送给Exchange
- 每个队列都要绑定到Exchange
- 生产者发送的消息经过Exchange到达队列，被多个消费者消费

exchange有以下几种类型：direct、topic、headers、fanout。在此我们主要研究fanout。

fanout类型下，Exchange会将所有它接收到的信息分发给和它绑定的队列。因此我们要做的就是绑定不同的队列到Exchange。

生产者

```java
public class Send {

    private static final String EXCHANGE_NAME = "exchage_fanout";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 声明Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        String msg = "hello";

        channel.basicPublish(EXCHANGE_NAME, "", null, msg.getBytes());

        channel.close();
        connection.close();
    }
}

```



消费者1

```java
public class Recv1 {

    private static final String QUEUE_NAME = "exchange_fanout_queue_1";

    private static final String EXCHANGE_NAME = "exchage_fanout";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到Exchange
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

        // 消费者在接收到队列里的消息但没有返回确认结果之前,它不会将新的消息分发给它
        channel.basicQos(1);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("[1] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println("[1] done");
                    // 手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 不自动应答
        boolean autoAck = false;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}

```



消费者2

```java
public class Recv2 {

    private static final String QUEUE_NAME = "exchange_fanout_queue_2";

    private static final String EXCHANGE_NAME = "exchage_fanout";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到Exchange
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

        // 消费者在接收到队列里的消息但没有返回确认结果之前,它不会将新的消息分发给它
        channel.basicQos(1);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("[2] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println("[2] done");
                    // 手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 不自动应答
        boolean autoAck = false;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}

```

结果输出：

> 消费者1：
>
> [1] recv: hello
> [1] done

> 消费者2：
>
> [2] recv: hello
> [2] done



## 路由Routing

发布/订阅模式下，只要和Exchange绑定的消费者，必须消费消息，这降低了灵活性。Routing模式的引入，可以提高这种灵活性，即使消费者和Exchange绑定，但是依旧可以通过引入“routingKey”来选择性消费消息。

只需将Exchange类型修改为direct，同时给消费者分配routingKey。

以下图为例，编写代码。

![](C:/Users/mercy/Desktop/images/python-four.png)

生产者

```java
public class Send {

    private static final String EXCHANGE_NAME = "exchange_direct";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 声明Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        String msg = "hello";

        String routingKey = "info";

        channel.basicPublish(EXCHANGE_NAME, routingKey, null, msg.getBytes());

        channel.close();
        connection.close();
    }
}

```



消费者1

```java
public class Recv1 {

    private static final String QUEUE_NAME = "exchange_direct_queue_1";

    private static final String EXCHANGE_NAME = "exchange_direct";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到Exchange,绑定error路由键
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "error");

        // 消费者在接收到队列里的消息但没有返回确认结果之前,它不会将新的消息分发给它
        channel.basicQos(1);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("[1] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println("[1] done");
                    // 手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 不自动应答
        boolean autoAck = false;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}

```



消费者2

```java
public class Recv2 {

    private static final String QUEUE_NAME = "exchange_direct_queue_2";

    private static final String EXCHANGE_NAME = "exchange_direct";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到Exchange,绑定error、info、warning路由键
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "error");
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "info");
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "warning");

        // 消费者在接收到队列里的消息但没有返回确认结果之前,它不会将新的消息分发给它
        channel.basicQos(1);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("[2] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println("[2] done");
                    // 手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 不自动应答
        boolean autoAck = false;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}

```

当生产者发送"info"时，消费者1和消费者2都能接收；当生产者发送"error"时，只有消费者2能接收。



## 主题Topics

某些情况下，我们可能无法列取出全部的routingKey，比如商品的操作，有很多，不会一一列举出来，但是我又只想绑定和商品有关的操作，这时需要用到Topics模式，这是一种比路由模式更加灵活的模式。

Topics模式是一种字符匹配模式，可通过如下通配符匹配：

- *可以代替任意一个字符
- #可以代替任意多个(包括0个)字符

以商品为例，`goods.#`表示和商品相关的所有操作，`goods.add`表示商品添加，`goods.delete`表示商品删除，编写代码如下。

生产者

```java
public class Send {

    private static final String EXCHANGE_NAME = "exchange_topic";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 声明Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        String msg = "hello";

        String routingKey = "goods.delete";

        channel.basicPublish(EXCHANGE_NAME, routingKey, null, msg.getBytes());

        channel.close();
        connection.close();
    }
}

```



消费者1

```java
public class Recv1 {

    private static final String QUEUE_NAME = "exchange_topic_queue_1";

    private static final String EXCHANGE_NAME = "exchange_topic";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到Exchange,绑定error路由键
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "goods.add");

        // 消费者在接收到队列里的消息但没有返回确认结果之前,它不会将新的消息分发给它
        channel.basicQos(1);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("[1] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println("[1] done");
                    // 手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 不自动应答
        boolean autoAck = false;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}

```



消费者2

```java
public class Recv2 {

    private static final String QUEUE_NAME = "exchange_topic_queue_2";

    private static final String EXCHANGE_NAME = "exchange_topic";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到Exchange,绑定error路由键
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "goods.#");

        // 消费者在接收到队列里的消息但没有返回确认结果之前,它不会将新的消息分发给它
        channel.basicQos(1);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("[2] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println("[2] done");
                    // 手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 不自动应答
        boolean autoAck = false;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}

```

生产者发送"goods.delete"，只有消费者2能接收。



## 消息确认机制

### 事务机制

txSelct

txCommit

txRollback



生产者

```java
public class Send {

    private static final String QUEUE_NAME = "simple_queue_tx";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        String msg = "hello";

        try {
            channel.txSelect();
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());

            int a = 1 / 0;

            channel.txCommit();
        } catch (Exception e) {
            channel.txRollback();
            System.out.println("rollback");
        }

        channel.close();
        connection.close();
    }
}

```



消费者

```java
public class Recv {

    private static final String QUEUE_NAME = "simple_queue_tx";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body,"UTF-8");
                System.out.println("recv: " + msg);
            }
        };

        // 监听队列
        channel.basicConsume(QUEUE_NAME, true, consumer);

    }
}

```

虽然这种模式很简单，但是降低了mq的吞吐量。



### Confirm机制

  修改代码片段

```java
channel.confirmSelect();

channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());

if (!channel.waitForConfirms()) {
    System.out.println("failed");
} else {
    System.out.println("ok");
}

```







































