package cn.huwhy.common.redis;

import redis.clients.jedis.Jedis;

/**
 * @author huwhy
 * @data 2016/12/5
 * @Desc
 */
public interface RedisAction<T> {

    T run(Jedis jedis);
}
