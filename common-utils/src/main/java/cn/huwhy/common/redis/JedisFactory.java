package cn.huwhy.common.redis;

import cn.huwhy.common.util.StringUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisFactory {

    private int maxActive = 1024;

    private int maxIdle = 64;

    private long maxWaitMillis = 6000;

    private JedisPool pool;

    private String host;

    private int port = 6379;

    private String password;

    //单位ms
    private int timeout = 15000;

    private Integer database;

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public void setMaxWaitMillis(long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setDatabase(Integer database) {
        this.database = database;
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
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);
        config.setTimeBetweenEvictionRunsMillis(30000);
        config.setNumTestsPerEvictionRun(10);
        if (StringUtil.isEmpty(password)) {
            pool = new JedisPool(config, host, port, timeout);
        } else if (database == null) {
            pool = new JedisPool(config, host, port, timeout, password);
        } else {
            pool = new JedisPool(config, host, port, timeout, password, database);
        }
    }

    public <T> T call(RedisAction<T> action) {
        Jedis resource = null;
        try {
            //循环7次去取可用链接
            int i = 0;
            do {
                try {
                    resource = pool.getResource();
                } catch (Exception ignore) {}
                i++;
            } while (resource == null && i < 7);
            if (resource != null) {
                return action.run(resource);
            } else {
                throw new NullPointerException("redis resource is null");
            }
        } finally {
            if (resource != null) {
                resource.close();
            }
        }
    }

}
