package com.feichaoyu.zookeeper.distributedlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author feichaoyu
 */
@RestController
public class TestController {

    private Logger log = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/test")
    public String zk() {
        try (ZkLock lock = new ZkLock()) {
            if (lock.lock("order")) {
                log.info("获取了锁");
                Thread.sleep(10_000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("释放了锁");
        return "success";
    }
}
