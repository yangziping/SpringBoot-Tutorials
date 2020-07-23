package com.feichaoyu.zookeeper.distributedlock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

/**
 * 使用zookeeper原生api实现分布式锁
 *
 * @author feichaoyu
 */
public class ZkLock implements AutoCloseable, Watcher {

    private ZooKeeper zooKeeper;

    private String znode;

    public ZkLock() throws IOException {
        this.zooKeeper = new ZooKeeper("localhost:2181", 10000, this);
    }

    public boolean lock(String bussinessCode) {
        try {
            // 创建业务根节点
            Stat stat = zooKeeper.exists("/" + bussinessCode, false);
            if (stat == null) {
                zooKeeper.create("/" + bussinessCode, bussinessCode.getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }

            // 创建瞬时有序节点 /order/order_00001
            znode = zooKeeper.create("/" + bussinessCode + "/" + bussinessCode + "_", bussinessCode.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);

            // 获取业务下所有子节点
            List<String> childrenNodes = zooKeeper.getChildren("/" + bussinessCode, false);
            // 子节点排序
            childrenNodes.sort(String::compareTo);
            // 获取第一个（序号最小的）子节点
            String firstChildNode = childrenNodes.get(0);

            // 如果创建的节点是第一个子节点，则获取到锁
            if (znode.endsWith(firstChildNode)) {
                return true;
            }
            // 否则每个节点监听自己的前一个节点，等待前一个节点的删除
            // PS:这里的前一个是指按插入顺序排序的，不一定按有序节点的顺序
            String prevChildNode = firstChildNode;
            for (String node : childrenNodes) {
                if (znode.endsWith(node)) {
                    // 监听触发process方法
                    zooKeeper.exists("/" + bussinessCode + "/" + prevChildNode, true);
                    break;
                } else {
                    prevChildNode = node;
                }
            }
            // 让线程在这等着，直到process方法触发后唤醒（也可以CountDownLatch）
            synchronized (this) {
                wait();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void unlock() throws Exception {
        zooKeeper.delete(znode, -1);
        zooKeeper.close();
    }

    @Override
    public void close() throws Exception {
        zooKeeper.delete(znode, -1);
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        // 监听前一个节点是否被删除
        if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
            synchronized (this) {
                notify();
            }
        }
    }
}
