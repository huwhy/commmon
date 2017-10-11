package cn.huwhy.common.redis;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import cn.huwhy.common.util.CollectionUtil;

public abstract class RedisQueue<T> {

    private Class<T> elementClass;

    protected JedisFactory jedisFactory;

    public RedisQueue() {
        Type type = this.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type actualType = ((ParameterizedType) type).getActualTypeArguments()[0];
            elementClass = (Class<T>) actualType;
        }
    }

    public void setJedisFactory(JedisFactory jedisFactory) {
        this.jedisFactory = jedisFactory;
    }

    public Long size(String queue) {
        return jedisFactory.call(jedis -> jedis.llen(queue));
    }

    public Long size() {
        return size(queueKey());
    }

    public Long push(String queue, String element) {
        return jedisFactory.call(jedis -> jedis.lpush(queue, element));
    }

    public Long push(String element) {
        return push(queueKey(), element);
    }

    public String pop(String queue, Integer seconds) {
        List<String> values = jedisFactory.call(jedis -> jedis.brpop(seconds, queue));
        if (CollectionUtil.isNotEmpty(values)) {
            return values.get(1);
        }
        return null;
    }

    public String pop(Integer seconds) {
        return pop(queueKey(), seconds);
    }

    public String transfer(String toQueue) {
        return jedisFactory.call(jedis -> jedis.brpoplpush(queueKey(), toQueue, 300));
    }

    public Long lrem(String element) {
        return jedisFactory.call(jedis -> jedis.lrem(queueKey(), 1, element));
    }

    public Long rrem(String element) {
        return jedisFactory.call(jedis -> jedis.lrem(queueKey(), -1, element));
    }

    public long length() {
        return jedisFactory.call(jedis -> jedis.llen(queueKey()));
    }

    public String last() {
        return jedisFactory.call(jedis -> jedis.lindex(queueKey(), -1L));
    }

    public String index(long index) {
        return jedisFactory.call(jedis -> jedis.lindex(queueKey(), index));
    }

    public String queueKey() {
        return "queue_" + elementClass.getName();
    }

    public static void main(String[] args) {
        JedisFactory factory = new JedisFactory();
        factory.setHost("127.0.0.1");
        factory.setPassword("abc123");
        factory.init();

    }
}
