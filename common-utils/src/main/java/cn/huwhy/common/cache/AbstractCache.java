package cn.huwhy.common.cache;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.huwhy.common.util.StringUtil;
import cn.huwhy.common.json.JsonUtil;
import cn.huwhy.common.redis.JedisFactory;

public abstract class AbstractCache<T, PK> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Class<T> tClass;

    /**
     * 秒
     */
    private int timeout = 30;

    JedisFactory jedisFactory;

    private Function<PK, String> keyFunc = t -> Integer.toString(t.hashCode());

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public JedisFactory getJedisFactory() {
        return jedisFactory;
    }

    public void setJedisFactory(JedisFactory jedisFactory) {
        this.jedisFactory = jedisFactory;
    }

    public void setKeyFunc(Function<PK, String> keyFunc) {
        this.keyFunc = keyFunc;
    }

    public AbstractCache() {
        Type t = getClass().getGenericSuperclass();
        if (t instanceof ParameterizedType) {
            Type[] p = ((ParameterizedType) t).getActualTypeArguments();
            tClass = (Class<T>) p[0];
        }
    }

    public String key(PK key) {
        return "cache:" + tClass.getName() + ":" + keyFunc.apply(key);
    }

    public T get(PK pk) {
        try {
            String key = key(pk);
            String value = jedisFactory.call(jedis -> jedis.get(key));
            logger.debug("cache get key:{}, value:{}", key, value);
            return JsonUtil.toObject(value, tClass);
        } catch (Exception e) {
            logger.error("redis cache:", e);
            return null;
        }
    }

    public void put(T entity, PK pk) {
        if (entity == null)
            return;
        try {
            String key = key(pk);
            String value = JsonUtil.toJson(entity);
            logger.debug("cache put key:{}, value:{}", key, value);
            jedisFactory.call(jedis -> {
                jedis.set(key, value);
                jedis.expire(key, timeout);
                return null;
            });
        } catch (Exception e) {
            logger.error("redis cache:", e);
        }
    }

    public void delByPk(PK pk) {
        del(key(pk));
    }

    public void del(String key) {
        try {
            jedisFactory.call(jedis -> jedis.del(key));
            logger.debug("cache del key:{}", key);
        } catch (Exception e) {
            logger.error("redis cache:", e);
        }
    }

    public void del(String... keys) {
        try {
            logger.debug("cache del keys:{}", keys);
            jedisFactory.call(jedis -> jedis.del(keys));
        } catch (Exception e) {
            logger.error("redis cache:", e);
        }
    }

    public void putString(String key, String value) {
        if (StringUtil.isEmpty(value) || StringUtil.isEmpty(key))
            return;
        try {
            logger.debug("cache put key:{}, value:{}", key, value);
            jedisFactory.call(jedis -> {
                jedis.set(key, value);
                jedis.expire(key, timeout);
                return null;
            });
        } catch (Exception e) {
            logger.error("redis cache:", e);
        }
    }

    public String getString(String key) {
        try {
            String value = jedisFactory.call(jedis -> jedis.get(key));
            logger.debug("cache get key:{}, value:{}", key, value);
            return value;
        } catch (Exception e) {
            logger.error("redis cache:", e);
            return null;
        }
    }

}
