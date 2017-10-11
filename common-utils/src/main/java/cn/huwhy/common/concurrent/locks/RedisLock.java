package cn.huwhy.common.concurrent.locks;

import java.util.List;

import cn.huwhy.common.util.CollectionUtil;
import cn.huwhy.common.util.ThreadUtil;
import cn.huwhy.common.redis.JedisFactory;

import redis.clients.jedis.Transaction;

public class RedisLock {

    private JedisFactory factory;

    /**
     * 秒
     */
    private int timeout = 60;

    public RedisLock() {}

    public RedisLock(JedisFactory factory) {
        this.factory = factory;
    }

    public void setFactory(JedisFactory factory) {
        this.factory = factory;
    }

    public Long lock(String key) {
        return tryLock(key, 0L);
    }

    public Long tryLock(String key, long timeoutMills) {
        return factory.call(redis -> {
            Long value = System.currentTimeMillis();
            boolean ok;
            do {
                ok = redis.setnx(key, Long.toString(value)) > 0;
                if (!ok) {
                    String s = redis.get(key);
                    if (s == null) continue;
                    Long oldValue = Long.valueOf(s);
                    if (value - oldValue > this.timeout * 1000L) {
                        redis.watch(key);
                        Transaction tx = redis.multi();
                        tx.setex(key, this.timeout + 2, Long.toString(value));
                        List<Object> res = tx.exec();
                        ok = CollectionUtil.isNotEmpty(res) && res.get(0).equals("OK");
                    }
                }
                if (!ok) {
                    ThreadUtil.sleep(100L);
                    if (System.currentTimeMillis() - value > timeoutMills) {
                        break;
                    }
                }
            } while (!ok);
            return ok ? value : 0;
        });
    }

    public void unLock(String key) {
        factory.call(redis -> redis.expire(key, 0));
    }

    /**
     * 秒
     *
     * @param timeout
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
