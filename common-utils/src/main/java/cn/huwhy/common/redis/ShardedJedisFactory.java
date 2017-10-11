package cn.huwhy.common.redis;

import java.util.List;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class ShardedJedisFactory {

    private int maxActive;

    private int maxIdle;

    private long maxWaitMillis;

    private Boolean testOnBorrow;

    private List<JedisShardInfo> shardInfos;

    private ShardedJedisPool pool;

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public void setMaxWaitMillis(Long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    public void setTestOnBorrow(Boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public void setShardInfos(List<JedisShardInfo> shardInfos) {
        this.shardInfos = shardInfos;
    }

    public void init() {
        JedisPoolConfig config = new JedisPoolConfig();
        if (maxActive > 0) {
            config.setMaxTotal(maxActive);
        }
        if (maxIdle > 0) {
            config.setMaxIdle(maxIdle);
        }
        if (maxWaitMillis > 0) {
            config.setMaxWaitMillis(maxWaitMillis);
        }
        if (testOnBorrow != null) {
            config.setTestOnBorrow(testOnBorrow);
        }
        pool = new ShardedJedisPool(config, shardInfos);
    }

    public ShardedJedis getJedis() {
        return pool.getResource();
    }

}
