package cn.huwhy.common.concurrent.locks;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import cn.huwhy.common.util.StringUtil;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class ZookeeperKeyLock {
    private static final int SESSION_TIMEOUT = 5000;

    private final String    node;
    private final String    path;
    private final ZooKeeper zk;

    private String truePath;
    private String waitPath;

    private CountDownLatch processLatch;

    public ZookeeperKeyLock(String hosts, String node, String key) throws Exception {
        this.node = node;
        this.path = "/" + node + "/" + key;

        CountDownLatch initLatch = new CountDownLatch(1);
        zk = new ZooKeeper(hosts, SESSION_TIMEOUT, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected)
                initLatch.countDown();

            if (event.getType() == Watcher.Event.EventType.NodeDeleted
                    && StringUtil.equals(event.getPath(), waitPath)) {
                try {
                    getOrder();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        initLatch.await();

        String nodePath = "/" + node;
        if (null == zk.exists(nodePath, false))
            zk.create(nodePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    public synchronized void lock() throws Exception {
        try {
            if (null != processLatch
                    || StringUtil.isNotEmpty(truePath))
                throw new Exception();

            processLatch = new CountDownLatch(1);
            truePath = zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            getOrder();

            processLatch.await();
        } catch (Exception e) {
            processLatch = null;
            truePath = null;
            waitPath = null;

            throw e;
        }
    }

    private void getOrder() throws Exception {
        String nodePath = "/" + node;
        List<String> childrenNodes = zk.getChildren(nodePath, false);
        if (childrenNodes.size() == 1) {
            processLatch.countDown();
        } else {
            Collections.sort(childrenNodes);
            String thisNode = truePath.substring(nodePath.length() + 1);
            int index = childrenNodes.indexOf(thisNode);
            if (index <= 0) {
                processLatch.countDown();
            } else {
                waitPath = nodePath + "/" + childrenNodes.get(index - 1);

                if (null == zk.exists(waitPath, true))
                    getOrder();
            }
        }
    }

    public synchronized void unlock() throws Exception {
        try {
            if (null == processLatch
                    || StringUtil.isEmpty(truePath))
                throw new Exception();

            zk.delete(truePath, -1);
        } finally {
            processLatch = null;
            truePath = null;
            waitPath = null;
        }
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    ZookeeperKeyLock zookeeperKeyLock = new ZookeeperKeyLock(
                            "localhost:2181",
                            "item_lock",
                            "itemId");
                    zookeeperKeyLock.lock();
                    try {
                        System.out.println(zookeeperKeyLock.toString());
                        Thread.sleep(1000);
                        System.out.println(zookeeperKeyLock.toString());
                    } finally {
                        zookeeperKeyLock.unlock();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
