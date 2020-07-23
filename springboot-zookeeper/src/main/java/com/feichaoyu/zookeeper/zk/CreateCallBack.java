package com.feichaoyu.zookeeper.zk;

import org.apache.zookeeper.AsyncCallback;

/**
 * @author feichaoyu
 */
public class CreateCallBack implements AsyncCallback.StringCallback {

    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        System.out.println("创建节点: " + path);
        System.out.println((String) ctx);
    }

}
