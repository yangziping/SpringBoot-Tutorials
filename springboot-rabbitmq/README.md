# AMQP

AMQP 是 Advanced Message Queuing Protocol 的简称，它是一个面向消息中间件的开放式标准应用层协议。AMQP 定义了如下这些特性：

- 消息方向
- 消息队列
- 消息路由（点对点和发布订阅模式）
- 可靠性
- 安全性

## 核心概念

**Server**：又称 Broker，接受客户端的连接，实现 AMQP 实体服务。

**Connection**：连接，应用程序与 Broker 的网络连接。

**Channel**：网络信道，几乎所有的操作都在 Channel 中进行，Channel 是进行消息读写的通道。客户端可建立多个 Channel，每个 Channel 代表一个会话任务。

**Message**：消息，服务器和应用程序之间传送的数据，由 Properties 和 Body 组成。Properties 可以对消息进行修饰，比如消息的优先级、延迟等高级特性，Body 则就是消息体内容。

**Virtual host**：虚拟地址，用于进行逻辑隔离，最上层的消息路由。一个 Virtual Host 里面可以有若干个Exchange 和 Queue，同一个 Virtual Host 里面不能有相同名称的 Exchange 或 Queue。

**Binding**：Exchange 和 Queue 之间的虚拟连接，binding 中可以包含 routing key。

**Routing key**：一个路由规则，虚拟机可用它来确定如何路由一个特定消息。

**Queue**：也称为 Message Queue，消息队列，保存消息并将它们转发给消费者。

## 消息路由

- 队列：点对点
  - 消息发送者发送消息，消息代理将其放入队列中，消息接收者从队列中获取消息内容，消息读取后被移出队列
  - 消息只有唯一的发送者和接收者
- 主题：发布/订阅
  - 发送者发送消息到主题，多个接收者订阅（监听）这个主题，那么就会在消息到达时同时接收消息

# RabbitMQ 入门

## 核心概念

### Exchange(交换机)

用于接收消息，并根据路由键转发消息所绑定的队列。

属性：

- **Name**：交换机名称
- **Type**：交换机类型，direct、topic、fanout、headers
- **Durability**：是否需要持久化，true 表示需要持久化
- **Auto Delete**：当最后一个绑定到 Exchange 上的队列删除后，自动删除该 Exchange 
- **Internal**：当前 Exchange 是否用于 RabbitMQ 内部使用，默认为 false
- **Arguments**：扩展参数，用于扩展 AMQP 协议自制定化使用

分析 Type 的 4 种类型：

**direct**：所有发送到 Direct Exchange 的消息被转发到 RouteKey 中指定的 Queue。

> 注意：direct 模式可以使用 RabbitMQ 自带的 default Exchange，所以不需要将 Exchange 进行任何绑定操作，消息传递时，RouteKey 必须完全匹配才会被队列接收，否则该消息会被抛弃。

**topic**：所有发送到 Topic Exchange 的消息被转发到所有关心 RouteKey 中指定 Topic 的 Queue 上。Exchange 将 RouteKey 和某 Topic 进行模糊匹配，此时队列需要绑定一个 Topic。

>注意：可以使用通配符进行模糊匹配
>
>符号 “#” 匹配一个或多个词
>
>符号 “ ” 匹配不多不少一个词
>
>例如："log.#"能够匹配到"log.info.oa"，"log.*"只会匹配到"log.erro"

**fanout**：不处理路由键，只需要简单的将队列绑定到交换机上，发送到交换机的消息都会被转发到与该交换机绑定的所有队列上，因此 fanout 交换机转发消息是最快的。

### Bingding(绑定)

Exchange 和 Exchange、Queue 之间的连接关系，Binding 中可以包含 RoutingKey 或者参数。

### Queue(消息队列)

消息队列，实际存储消息数据。

Durability：是否持久化，Durable：是，Transient：否。

Auto delete：如选 yes，代表当最后一个监听被移除之后，该 Queue 会自动被删除。

### Message(消息)

服务器和应用程序之间传送的数据。

本质上就是一段数据，由 Properties 和 Payload Body 组成。

### Virtual host(虚拟主机)

虚拟地址，用于进行逻辑隔离，最上层的消息路由。

一个 Virtual Host 里面可以有若干个 Exchang e和 Queue。

同一个 Virtual Host 里面不能有相同名称的 Exchange 或 Queue。

## 持久化

### 队列持久化

```java
boolean durable = true;
channel.queueDeclare("task_queue", durable, false, false, null);
```

### 消息持久化

```java
channel.basicPublish("", "task_queue",
            MessageProperties.PERSISTENT_TEXT_PLAIN,
            message.getBytes());
```

## 简单队列

**生产者**

```java
public class Send {

    private static final String QUEUE_NAME = "simple_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        String msg = "hello";

        channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());

        channel.close();
        connection.close();
    }
}
```

**消费者**

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
                String msg = new String(body, StandardCharsets.UTF_8);
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

**生产者**

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

**消费者1**

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
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("[1] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println("[1] done");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 自动应答
        boolean autoAck = true;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}
```

**消费者2**

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
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("[2] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println("[2] done");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 自动应答
        boolean autoAck = true;
        // 监听队列
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }
}
```

结果输出：

```
消费者1:
[1] recv: hello0
[1] done
[1] recv: hello2
[1] done
[1] recv: hello4
[1] done
[1] recv: hello6
[1] done
[1] recv: hello8
[1] done

消费者2:
[2] recv: hello1
[2] done
[2] recv: hello3
[2] done
[2] recv: hello5
[2] done
[2] recv: hello7
[2] done
[2] recv: hello9
[2] done
```

从结果看出，消费者1 和消费者2 都是消费 5 个消息，但我们想让消费者1 消费的数量是消费者2 的一半，因为消费者1 效率低，消费者2 效率高，所谓能者多劳，就是这个意思。

### 公平消费

关闭自动应答 `autoAck=false`，加上 `channel.basicQos(1)`，表示该消费者在接收到队列里的消息但没有返回确认结果之前，它不会将新的消息分发给它。

**消费者1**

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

        // 消费者在接收到队列里的消息但没有返回确认结果之前，mq不会将新的消息再次分发给消费者
        channel.basicQos(1);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
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

**消费者2**

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

        // 消费者在接收到队列里的消息但没有返回确认结果之前，mq不会将新的消息再次分发给消费者
        channel.basicQos(1);

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("[2] recv: " + msg);
                try {
                    TimeUnit.SECONDS.sleep(1);
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

```
消费者1:
[1] recv: hello0
[1] done
[1] recv: hello3
[1] done
[1] recv: hello6
[1] done
[1] recv: hello9
[1] done

消费者2:
[2] recv: hello1
[2] done
[2] recv: hello2
[2] done
[2] recv: hello4
[2] done
[2] recv: hello5
[2] done
[2] recv: hello7
[2] done
[2] recv: hello8
[2] done
```

可以看出，消费者2 处理消息数量是消费者1 的两倍。

### 消息应答

`autoAck = true` 表示开启自动确认模式，一旦 rabbitmq 将消息分发给消费者，就会从内存中删除消息，这种情况下，如果杀死正在执行的消费者，就会丢失正在处理的消息。

`autoAck = false` 表示开启手动确认模式，如果有一个消费者挂掉，就会交付给其他消费者，rabbitmq 支持消息应答，消费者发送一个消息应答，告诉 rabbitmq 这个消息已经处理完，可以删了，然后 rabbitmq 就删除内存中的消息。

## 发布/订阅

以上两种模式下，一个消息只能发送给一个消费者，如何将一个消息发送给多个消费者呢？就是下面要介绍的发布/订阅模式。

![](../images/python-three.png)

对上图的解释：

- 一个生产者，多个消费者
- 每个消费者都有自己的队列
- 生产者没有直接把消息发送到队列，而是发送给 Exchange
- 每个队列都要绑定到 Exchange
- 生产者发送的消息经过 Exchange 到达队列，被多个消费者消费

Exchange 有以下几种类型：direct、topic、headers、fanout。在此我们主要研究 `fanout`。

fanout 类型下，Exchange 会将所有它接收到的信息分发给和它绑定的队列。因此我们要做的就是绑定不同的队列到 Exchange。

**生产者**

```java
public class Send {

    private static final String EXCHANGE_NAME = "exchange_fanout";

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

**消费者1**

```java
public class Recv1 {

    private static final String QUEUE_NAME = "exchange_fanout_queue_1";

    private static final String EXCHANGE_NAME = "exchange_fanout";

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
                String msg = new String(body, StandardCharsets.UTF_8);
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

**消费者2**

```java
public class Recv2 {

    private static final String QUEUE_NAME = "exchange_fanout_queue_2";

    private static final String EXCHANGE_NAME = "exchange_fanout";

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
                String msg = new String(body, StandardCharsets.UTF_8);
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

```
消费者1:
[1] recv: hello
[1] done

消费者2:
[2] recv: hello
[2] done
```

## 路由 Routing

发布/订阅模式下，只要和 Exchange 绑定的消费者，必须消费消息，这降低了灵活性。Routing 模式的引入，可以提高这种灵活性，即使消费者和 Exchange 绑定，但是依旧可以通过引入 routingKey 来选择性消费消息。

只需将 Exchange 类型修改为 `direct`，同时给消费者分配 routingKey 。

以下图为例，编写代码。

![](../images/python-four.png)

**生产者**

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

**消费者1**

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
                String msg = new String(body, StandardCharsets.UTF_8);
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

**消费者2**

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
                String msg = new String(body, StandardCharsets.UTF_8);
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

当生产者发送 "error" 时，消费者1 和消费者2 都能接收；当生产者发送 "info" 或 "warning" 时，只有消费者2 能接收。

## 主题 Topics

某些情况下，我们可能无法列取出全部的 routingKey，比如商品的操作，有很多，不会一一列举出来，但是又只想绑定和商品有关的操作，这时需要用到 Topics 模式，这是一种比路由模式更加灵活的模式。

只需将 Exchange 类型修改为 `topic`，就可以使用 Topics 模式。

Topics 模式是一种字符匹配模式，可通过如下通配符匹配：

- *可以代替任意一个字符
- #可以代替任意多个（包括 0 个）字符

以商品为例，`goods.#` 表示和商品相关的所有操作，`goods.add` 表示商品添加，`goods.delete` 表示商品删除，编写代码如下。

**生产者**

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

**消费者1**

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
                String msg = new String(body, StandardCharsets.UTF_8);
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

**消费者2**

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
                String msg = new String(body, StandardCharsets.UTF_8);
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

生产者发送 "goods.delete"，只有消费者2 能接收。

# 深入 RabbitMQ

## 消息如何保证100%投递成功

**生产端的可靠性投递**

- 保障消息的成功发出

- 保障 MQ 节点的成功接收

- 发送端收到消息确认应答
- 完善的消息进行补偿机制

**互联网大厂的解决方案**

- 消息落库，对消息状态进行打标

![](https://cdn.jsdelivr.net/gh/FeiChaoyu/cdn/img/2020-04-16_13-53-44.jpg)

**step1**：业务进入业务 DB，然后消息进入消息 DB，消息记录表中会存储一个 status 字段表示该消息是否被成功消费，成功消费为 1。会对数据库进行两次插入，一次业务，一次消息记录。（中间可以考虑快速失败）

**step2**：生产者发送消息给 broker。

**step3**：生产者异步监听消息回复。

**step4**：生产者收到消息回复，则将数据库中的 status 字段修改为 1，表示成功消费。

**step5**：如果在 **step3** 处，生产者一直没有收到消息回复，则此时会有一个分布式定时任务定时抓取消息记录，判断 status 字段是否为 0。

**step6**：如果 status 字段为 0，则分布式定时任务会将消息重发。

**step7**：如果重试次数超过一定值，则修改 status 字段为 2，表示消息投递失败，此时会有一个补偿系统去查询这些失败的消息，做出相应的补偿。



- 消息的延迟投递，做二次检查，回调检查

![](https://cdn.jsdelivr.net/gh/FeiChaoyu/cdn/img/2020-04-16_14-41-23.jpg)

**step1**：先将业务数据落库，然后发送消息。

**step2**：再发一个延时消息用于投递检查。

**step3**：消费者监听队列，处理消息。

**step4**：消费者发送消息回执。

**step5**：callback 服务监听消息回执，收到后对消息进行落库。

**step6**：callback 服务监听延时消息，收到后去消息 DB 中查询是否已经存在该消息，如果不存在，则 callback 服务会对生产者发起一次 RPC，要求生产者重发消息。

这个方案的优点在于：高并发下只需要对数据库进行一次业务落库操作，整个链路无需关心消息存储。由外部系统异步处理消息罗库。实际上，callback 服务作为外部补偿系统与核心链路进行了解耦。

## 幂等性

**在海量订单产生的业务高峰期，如何避免消息的重复消费问题？**

消费端实现幂等性，就意味着，我们的消息永远不会消费多次，即使我们收到了多条一样的消息。

**业界主流的幂等性操作：**

- 唯一 ID + 指纹码机制，利用数据库主键去重

`SELECT COUNT(1) FROM T_ORDER WHERE ID=唯一ID+指纹码`

好处：实现简单

坏处：高并发下有数据库写入的性能瓶颈

解决方案：跟进 ID 进行分库分表进行算法路由



- 利用 Redis 的原子性去实现

使用 Redis 进行幂等，需要考虑的问题

第一：我们是否要进行数据落库，如果落库的话，关键解决的问题是数据库和缓存如何做到原子性？

第二：如果不进行落库，那么都存储到缓存中，如何设置定时同步的策略？

## 事务机制

```java
channel.txSelect();

channel.txCommit();

channel.txRollback();
```

**生产者**

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

**消费者**

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
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("recv: " + msg);
            }
        };

        // 监听队列
        channel.basicConsume(QUEUE_NAME, true, consumer);

    }
}
```

虽然这种模式很简单，但是降低了 rabbitmq 的吞吐量，因为需要大量 commit。

## Confirm 确认消息

**消息的确认**是指生产者投递消息后，如果 Broker 收到消息，则会给生产者一个应答。生产者进行接收应答，用来确定这条消息是否正常的发送到 Broker，这种方式也是消息的可靠性投递的核心保障。

**如何实现 Confirm 确认消息？**

1. 在 channel 上开启确认模式：`channel.confirmSelect()`
2. 在 channel 上添加监听：`addConfirmListener`，监听成功和失败的返回结果，根据具体的结果对消息进行重新发送或记录日志等后续处理。

**生产者**

```java
public class Send {

    private static final String QUEUE_NAME = "simple_queue_confirm";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        String msg = "hello";

        channel.confirmSelect();

        channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());

        // 方法1
        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("ack");
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("no ack");
            }
        });

        // 方法2
        if (!channel.waitForConfirms()) {
            System.out.println("failed");
        } else {
            System.out.println("ok");
        }

        channel.close();
        connection.close();
    }
}
```

**消费者**

```java
public class Recv {

    private static final String QUEUE_NAME = "simple_queue_confirm";

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
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("recv: " + msg);
            }
        };

        // 监听队列
        channel.basicConsume(QUEUE_NAME, true, consumer);

    }
}
```

## Return 消息机制

**Return Listener** 用于处理一些不可路由的消息。

消息生产者通过指定一个 exchange 和 routingkey，把消息发送到队列中，然后消费者监听队列，进行消费处理操作。但是在某些情况下，如果在发送消息的时候，当前的 exchange 不存在或者指定的 routingkey 路由不到，这个时候如果我们需要监听这种不可达的消息，就要使用 **Return Listener**。

在基础 API 中有一个关键的配置项：

`Mandatory`：如果为 true，则监听器会接收到路由不可达的消息，然后进行后续处理，如果为 false，那么broker 端会自动删除该消息。

**生产者**

```java
public class Send {

    private static final String EXCHANGE_NAME = "exchange_topic";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        String msg = "hello";
        String routingKey = "return.save";
        String routingKeyError = "abc.save";

        channel.confirmSelect();

        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("------handle return------");
                System.out.println("replyCode:" + replyCode);
                System.out.println("replyText:" + replyText);
                System.out.println("exchange:" + exchange);
                System.out.println("routingKey:" + routingKey);
                System.out.println("properties:" + properties);
                System.out.println("body:" + new String(body));
            }
        });

        // 第三个参数为mandatory
        // true表示监听器会接收到路由不可达的消息，然后进行后续处理
        // false表示broker端会自动删除该消息
//        channel.basicPublish(EXCHANGE_NAME, routingKey, true, null, msg.getBytes());
        channel.basicPublish(EXCHANGE_NAME, routingKeyError, true, null, msg.getBytes());

//        channel.close();
//        connection.close();
    }
}
```

**消费者**

```java
public class Recv {

    private static final String QUEUE_NAME = "test_return_listener";
    private static final String EXCHANGE_NAME = "exchange_topic";


    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 声明Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        // 创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到Exchange，绑定路由键
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "return.#");

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("recv: " + msg);
            }
        };

        // 监听队列
        channel.basicConsume(QUEUE_NAME, true, consumer);

    }
}
```

## 消费端限流

假设一个场景，RabbitMQ 服务器有上万条未处理的消息，我们随便打开一个消费者客户端，会出现下面情况：巨量的消息瞬间全部推送过来，但是我们单个客户端无法同时处理这么多数据。

RabbitMQ 提供了一种 qos（服务质量保证）功能，即在非自动确认消息的前提下，如果一定数目的消息（通过基于 consume 或者 channel 设置 qos 的值）未被确认前，不进行消费新的消息。

RabbitMQ 提供了如下的方法：

`void basicQos(int prefetchSize, int prefetchCount, boolean global)`

- prefetchSize：0
- prefetchCount：会告诉 RabbitMQ 不要同时给一个消费者推送多于 N 个消息，即一旦有 N 个消息还没有ack，则该 consumer 将 block 掉，直到有消息 ack
- global：true/false，表示是否将上面设置应用于 channel。简单点说，就是上面限制是 channel 级别的还是consumer 级别
- prefetchSize 和 global 这两项，rabbitmq 没有实现，暂且不研究 prefetch_count 在 no_ask=false 的情况下生效，即在自动应答的情况下这两个值是不生效的

代码见上面的[公平消费](# 公平消费)

## 消费端 ACK 与重回队列

消费端进行消费的时候，如果由于业务异常我们可以进行日志的记录，然后进行补偿。如果由于服务器宕机等严重问题，那我们就需要手工进行 ACK 保障消费端消费成功。

消费端重回队列是为了对没有处理成功的消息，把消息重新投递给 broker。

RabbitMQ 提供了如下的方法：

`void basicNack(long deliveryTag, boolean multiple, boolean requeue)`

一般我们在实际应用中，都会关闭重回队列，也就是将 `requeue` 设置为 false。

## TTL 队列/消息

TTL(Time To Live)是指生存时间。

RabbitMQ 支持消息的过期时间，在消息发送时可以进行指定。

RabbitMQ 支持队列的过期时间，从消息入队列开始计算，只要超过了队列的超时时间配置，那么消息会自动清除。

## 死信队列(Dead-Letter-Exchange)

利用 DLX，当消息在一个队列中变成死信(dead message)之后，它能被重新 publish 到另一个 Exchange，这个 Exchange 就是 DLX。

**消息变成死信有以下几种情况**：

- 消息被拒绝（`basic.reject`、`basic.nack`）并且 requeue = false

- 消息 TTL 过期
- 队列达到最大长度

DLX 也是一个正常的 Exchange，和一般的 Exchange 没有区别，它能在任何的队列上被指定，实际上就是设置某个队列的属性。

当这个队列中有死信时，RabbitMQ 就会自动的将这个消息重新发布到设置的 Exchange 上去，进而被路由到另一个队列。

可以监听这个队列中消息做相应的处理，这个特性可以弥补 RabbitMQ 3.0 以前支持的 immediate 参数的功能。

**生产者**

```java
public class Send {

    private static final String EXCHANGE_NAME = "test_dlx_exchange";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        String msg = "hello";
        String routingKey = "dlx.save";

        channel.confirmSelect();

        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .deliveryMode(2)
                .contentType(StandardCharsets.UTF_8.toString())
                // 设置消息过期时间为10s
                .expiration("10000")
                .build();
        channel.basicPublish(EXCHANGE_NAME, routingKey, properties, msg.getBytes());

    }
}
```



**消费者**

```java
public class Recv {

    private static final String QUEUE_NAME = "test_dlx_queue";
    private static final String EXCHANGE_NAME = "test_dlx_exchange";


    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();

        // 获取通道
        Channel channel = connection.createChannel();

        // 声明正常的Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "dlx.exchange");

        // 创建正常的队列
        channel.queueDeclare(QUEUE_NAME, true, false, false, arguments);

        // 绑定正常的队列到Exchange，绑定路由键
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "dlx.#");

        // 进行死信队列的声明
        channel.exchangeDeclare("dlx.exchange", "topic");
        channel.queueDeclare("dlx.queue", true, false, false, null);
        channel.queueBind("dlx.queue", "dlx.exchange", "#");

        // 定义消费者DeliverCallback
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("recv: " + msg);
            }
        };

        // 监听队列
        channel.basicConsume(QUEUE_NAME, true, consumer);

    }
}
```

我们先启动消费者，这时会有创建两个队列，如下所示，然后停止消费者，启动生产者，可以发现，刚开始有一条消息进入 `test_dlx_queue`，然后过了 10s 后，消息进入死信队列 `dlx_queue`。

![](https://cdn.jsdelivr.net/gh/FeiChaoyu/cdn/img/Snipaste_2020-04-16_22-58-16.png)

# Spring Boot 整合 RabbitMQ

由 `RabbitAutoConfiguration` 可知，Spring Boot 自动帮我们注入了 `CachingConnectionFactory`、`RabbitTemplate`、`AmqpAdmin`。

**application.yml**

```yml
spring:
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: fcy
    password: 123
    # 确认消息成功发送，成功会进行回调
    publisher-confirms: true
    # 对于没有发送成功的消息，会进行回调
    publisher-returns: true
    # 这个按照自己需求配置，默认不配置是 /
    virtual-host: /fcy

# 自定义消息队列名称
rabbitmq:
  queue:
    msg: spring-boot-queue-msg
    user: spring-boot-queue-user
    routingKey: rabbit.#
    topic: my-topic
```

`MsgQueue` 中配置了消费者队列、交换机、绑定规则。

```java
@Configuration
public class MsgQueue {

    // 消息队列名称
    @Value("${rabbitmq.queue.msg}")
    private String msgQueueName = null;

    // 消息队列路由键
    @Value("${rabbitmq.queue.routingKey}")
    private String msgRoutingKey = null;

    // 消息队列主题
    @Value("${rabbitmq.queue.topic}")
    private String msgTopic = null;

    // 用户队列名称
    @Value("${rabbitmq.queue.user}")
    private String userQueueName = null;

    @Bean
    public Queue createQueueMsg() {
        // 创建字符串消息队列，boolean值代表是否持久化消息
        return new Queue(msgQueueName, true);
    }

    @Bean
    public Queue createQueueUser() {
        // 创建用户消息队列，boolean值代表是否持久化消息
        return new Queue(userQueueName, true);
    }

    @Bean
    public TopicExchange exchange() {
        // 创建交换机，类型是topic，不持久化，不自动删除
        return new TopicExchange(msgTopic, false, false);
    }

    @Bean
    public Binding createBinding() {
        // 将队列以路由键rabbit.*的形式绑定到交换机
        return BindingBuilder.bind(createQueueMsg()).to(exchange()).with(msgRoutingKey);
    }
}
```

具备回调功能的生产者

```java
@Service
public class RabbitMqServiceImpl
        // 实现ConfirmCallback接口和ReturnCallback接口，进行回调
        implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback, RabbitMqService {

    private String msgRouting = "rabbit.hello.world";

    @Value("${rabbitmq.queue.topic}")
    private String msgTopic = null;

    @Value("${rabbitmq.queue.user}")
    private String userRouting = null;

    @Autowired
    private RabbitTemplate rabbitTemplate = null;

    @Override
    public void sendMsg(String msg) {
        System.out.println("发送消息: 【" + msg + "】");
        // 设置回调
        rabbitTemplate.setConfirmCallback(this);
        // 设置消息
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setConsumerTag("consumer1");
        Message message = new Message(msg.getBytes(), messageProperties);
        // 发送消息，MessagePostProcessor是发送消息后置处理器
        rabbitTemplate.convertAndSend(msgTopic, msgRouting, message, message1 -> {
            System.out.println(message1.getMessageProperties().getConsumerTag());
            return message1;
        });
    }

    @Override
    public void sendUser(User user) {
        System.out.println("发送用户消息: 【" + user + "】");
        // 设置回调
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.convertAndSend(userRouting, user);
    }

    /**
     * 回调发送成功方法
     *
     * @param correlationData
     * @param ack
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData,
                        boolean ack, String cause) {
        if (ack) {
            System.out.println("消息成功消费");
        } else {
            System.out.println("消息消费失败:" + cause);
        }
    }

    /**
     * 回调发送失败方法
     *
     * @param message
     * @param i
     * @param s
     * @param s1
     * @param s2
     */
    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {
        System.out.println("return exchange: " + s1 + ", routingKey: " + s2 + ", replyCode: " + i + ", replyText: " + s);
    }
}
```

消费者接收消息

```java
@Component
public class RabbitMessageReceiver {

    /**
     * 定义监听字符串队列名称
     *
     * @param message
     */
    @RabbitListener(queues = {"${rabbitmq.queue.msg}"})
    public void receiveMsg(Message message) {
        System.out.println("收到消息: 【" + new String(message.getBody()) + "】");
    }

    /**
     * 定义监听用户队列名称
     *
     * @param user
     */
    @RabbitListener(queues = {"${rabbitmq.queue.user}"})
    public void receiveUser(User user) {
        System.out.println("收到用户信息【" + user + "】");
    }
}
```

启动工程，在浏览器中输入 `localhost:8080/rabbitmq/msg?message=hello`