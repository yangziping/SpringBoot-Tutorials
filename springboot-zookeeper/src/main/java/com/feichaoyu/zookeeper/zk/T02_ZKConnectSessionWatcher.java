package com.feichaoyu.zookeeper.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author feichaoyu
 */
public class T02_ZKConnectSessionWatcher implements Watcher {

    final static Logger log = LoggerFactory.getLogger(T02_ZKConnectSessionWatcher.class);

    public static final String zkServerPath = "localhost:2181";
    public static final Integer timeout = 5000;

    public static void main(String[] args) throws Exception {

        ZooKeeper zk = new ZooKeeper(zkServerPath, timeout, new T02_ZKConnectSessionWatcher());

        long sessionId = zk.getSessionId();
        String ssid = "0x" + Long.toHexString(sessionId);
        System.out.println(ssid);
        byte[] sessionPassword = zk.getSessionPasswd();

        log.info("客户端开始连接zookeeper服务器...");
        log.info("连接状态：{}", zk.getState());
        new Thread().sleep(1000);
        log.info("连接状态：{}", zk.getState());

        new Thread().sleep(200);

        // 开始会话重连
        log.info("开始会话重连...");

        ZooKeeper zkSession = new ZooKeeper(zkServerPath,
                timeout,
                new T02_ZKConnectSessionWatcher(),
                sessionId,
                sessionPassword);
        log.info("重新连接状态zkSession：{}", zkSession.getState());
        new Thread().sleep(1000);
        log.info("重新连接状态zkSession：{}", zkSession.getState());
    }

    @Override
    public void process(WatchedEvent event) {
        log.info("接受到watch通知：{}", event);
    }
}


