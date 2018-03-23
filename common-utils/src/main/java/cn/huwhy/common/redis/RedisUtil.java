package cn.huwhy.common.redis;

import cn.huwhy.common.util.SerializationUtil;
import cn.huwhy.common.util.ThreadUtil;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;

public class RedisUtil extends JedisFactory {

    private static final Long OK = 1L;

    /**
     * 列出redis所有的键
     *
     * @param keyPattern
     * @return
     */
    public Set<String> keys(final String keyPattern) {
        return this.call(new RedisAction<Set<String>>() {
            @Override
            public Set<String> run(Jedis jedis) {
                return jedis.keys(keyPattern);
            }
        });
    }

    public <T> boolean set(String key, T object, int seconds) {
        return this.call(new RedisAction<Boolean>() {
            @Override
            public Boolean run(Jedis jedis) {
                try {
                    if (seconds > 0) {
                        jedis.setex(key.getBytes(), seconds, SerializationUtil.serialize(object));
                    } else {
                        jedis.set(key.getBytes(), SerializationUtil.serialize(object));
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
    }

    public <T> T get(String key, Class<T> cls) {
        return this.call(new RedisAction<T>() {
            @Override
            public T run(Jedis jedis) {
                byte[] value = jedis.get(key.getBytes());
                return SerializationUtil.unserialize(value, cls);
            }
        });
    }

    public long getExpireSeconds(String key) {
        return this.call(new RedisAction<Long>() {
            @Override
            public Long run(Jedis jedis) {
                return jedis.ttl(key.getBytes());
            }
        });
    }

    public boolean del(String key) {
        return this.call(new RedisAction<Boolean>() {
            @Override
            public Boolean run(Jedis jedis) {
                jedis.del(key.getBytes());
                return true;
            }
        });
    }

    public long delRegEx(String keyReg) {
        return this.call(new RedisAction<Long>() {
            @Override
            public Long run(Jedis jedis) {
                Set<String> keys = jedis.keys(keyReg);
                long cnt = 0;
                if (keys != null && !keys.isEmpty()) {
                    for (String key : keys) {
                        jedis.del(key.getBytes());
                        cnt++;
                    }
                }
                return cnt;
            }
        });
    }

    public boolean exists(String key) {
        return this.call(new RedisAction<Boolean>() {
            @Override
            public Boolean run(Jedis jedis) {
                return jedis.exists(key);
            }
        });
    }

    public long expire(String key, int seconds) {
        return this.call(new RedisAction<Long>() {
            @Override
            public Long run(Jedis jedis) {
                return jedis.expire(key.getBytes(), seconds);
            }
        });
    }

    public boolean setNx(String key, Object obj, int seconds) {
        return this.call(new RedisAction<Boolean>() {
            @Override
            public Boolean run(Jedis jedis) {
                Long res = jedis.setnx(key, obj.toString());
                if (OK.equals(res)) {
                    jedis.expire(key.getBytes(), seconds);
                    return true;
                }
                return false;
            }
        });
    }

    public Long incr(String key) {
        return this.call(new RedisAction<Long>() {
            @Override
            public Long run(Jedis jedis) {
                return jedis.incr(key.getBytes());
            }
        });
    }

    public Long incrBy(String key, long v) {
        return this.call(new RedisAction<Long>() {
            @Override
            public Long run(Jedis jedis) {
                return jedis.incrBy(key.getBytes(), v);
            }
        });
    }

    public Long decr(String key) {
        return this.call(new RedisAction<Long>() {
            @Override
            public Long run(Jedis jedis) {
                return jedis.decr(key.getBytes());
            }
        });
    }

    public Long decrBy(String key, long v) {
        return this.call(new RedisAction<Long>() {
            @Override
            public Long run(Jedis jedis) {
                return jedis.decrBy(key.getBytes(), v);
            }
        });
    }

    public boolean hSet(String key, String field, Object obj) {
        return hSet(key, field, obj, -1);
    }

    public boolean hSet(String key, String field, Object obj, int seconds) {
        return this.call(new RedisAction<Boolean>() {
            @Override
            public Boolean run(Jedis jedis) {
                jedis.hset(key.getBytes(), field.getBytes(), SerializationUtil.serialize(obj));
                if (seconds > 0) {
                    jedis.expire(key.getBytes(), seconds);
                }
                return true;
            }
        });
    }

    public <T> T hGet(String key, String field, Class<T> cls) {
        return this.call(new RedisAction<T>() {
            @Override
            public T run(Jedis jedis) {
                return SerializationUtil.unserialize(jedis.hget(key.getBytes(), field.getBytes()), cls);
            }
        });
    }

    public <T> boolean lpush(String key, T object, int second) {
        return this.call(new RedisAction<Boolean>() {
            @Override
            public Boolean run(Jedis jedis) {
                byte[] bytes = SerializationUtil.serialize(object);
                Long ret = jedis.lpush(key.getBytes(), bytes);
                if (second > 0) {
                    jedis.expire(key, second);
                }
                return ret > 0;
            }
        });
    }

    public <T> List<T> lrange(String key, int start, int end, Class<T> cls) {
        return this.call(new RedisAction<List<T>>() {
            @Override
            public List<T> run(Jedis jedis) {
                return SerializationUtil.unserialize(jedis.lrange(key.getBytes(), start, end), cls);
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        RedisUtil util = new RedisUtil();
        util.setHost("127.0.0.1");
        util.setPassword("abc123");
        util.init();

        String key = "goods_stock";
        util.setNx(key, 1000, 0);

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                  while(true) {
                     long stock = util.decr(key);
                     if (stock >= 0) {
                         System.out.println(Thread.currentThread().getName() + ": 抢购成功");
                     } else {
                         util.incr(key);
                         break;
                     }
                     ThreadUtil.sleep(50L);
                  }
                }
            }).start();
        }

        do {
            ThreadUtil.sleep(5000L);
        } while (util.get(key, int.class) > 0);
    }

}
