package com.feichaoyu.zookeeper.zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;

/**
 * @author feichaoyu
 */
public class T03_ZKNodeOperator implements Watcher {

    private ZooKeeper zookeeper = null;

    public static final String zkServerPath = "localhost:2181";
    public static final Integer timeout = 5000;

    public T03_ZKNodeOperator() {
    }

    public T03_ZKNodeOperator(String connectString) {
        try {
            zookeeper = new ZooKeeper(connectString, timeout, new T03_ZKNodeOperator());
        } catch (IOException e) {
            e.printStackTrace();
            if (zookeeper != null) {
                try {
                    zookeeper.close();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void createZKNode(String path, byte[] data, List<ACL> acls) {

        String result = "";
        try {
            /*
             * 同步或者异步创建节点，都不支持子节点的递归创建，异步有一个callback函数
             * 参数：
             * path：创建的路径
             * data：存储的数据的byte[]
             * acl：控制权限策略
             * 			Ids.OPEN_ACL_UNSAFE --> world:anyone:cdrwa
             * 			CREATOR_ALL_ACL --> auth:user:password:cdrwa
             * createMode：节点类型, 是一个枚举
             * 			PERSISTENT：持久节点
             * 			PERSISTENT_SEQUENTIAL：持久顺序节点
             * 			EPHEMERAL：临时节点
             * 			EPHEMERAL_SEQUENTIAL：临时顺序节点
             */
            result = zookeeper.create(path, data, acls, CreateMode.PERSISTENT);

//			String ctx = "{'create':'success'}";
//			zookeeper.create(path, data, acls, CreateMode.PERSISTENT, new CreateCallBack(), ctx);

            System.out.println("创建节点：\t" + result + "\t成功...");
            new Thread().sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        T03_ZKNodeOperator zkServer = new T03_ZKNodeOperator(zkServerPath);

        // 创建zk节点
//		zkServer.createZKNode("/testnode", "testnode".getBytes(), Ids.OPEN_ACL_UNSAFE);

        // 修改zk节点
//		Stat status  = zkServer.getZookeeper().setData("/testnode", "xyz".getBytes(), 2);
//		System.out.println(status.getVersion());

        zkServer.createZKNode("/test-delete-node", "123".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE);
//		zkServer.getZookeeper().delete("/test-delete-node", 0);

        String ctx = "{'delete':'success'}";
        zkServer.getZookeeper().delete("/test-delete-node", 0, new DeleteCallBack(), ctx);
        Thread.sleep(2000);
    }

    public ZooKeeper getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(ZooKeeper zookeeper) {
        this.zookeeper = zookeeper;
    }

    @Override
    public void process(WatchedEvent event) {
    }
}


