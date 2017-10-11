package cn.huwhy.common.bees;

import com.alibaba.fastjson.JSON;
import cn.huwhy.common.redis.JedisFactory;

import cn.huwhy.bees.common.RequestState;
import cn.huwhy.bees.core.extension.SpiMeta;
import cn.huwhy.bees.rpc.DefaultResponse;
import cn.huwhy.bees.rpc.Request;
import cn.huwhy.bees.transport.RequestIdempotentManager;

/**
 * @author huwhy
 * @data 2016/11/25
 * @Desc
 */
@SpiMeta(name = "redis")
public class RedisIdempotentManager implements RequestIdempotentManager {

    protected JedisFactory jedisFactory;

    public void setJedisFactory(JedisFactory jedisFactory) {
        this.jedisFactory = jedisFactory;
    }

    @Override
    public RequestState state(Request request) {
        final String key = request.getRequestId() + request.getInterfaceName();
        String stateName = jedisFactory.call(jedis -> jedis.get(key));
        try {
            return RequestState.valueOf(stateName);
        } catch (Exception e) {
            return RequestState.NONE;
        }
    }

    @Override
    public void doing(Request request) {
        String key = request.getRequestId() + request.getInterfaceName();
        jedisFactory.call(jedis -> {
            jedis.set(key, RequestState.DOING.name());
            jedis.expire(key, 15 * 60);
            return null;
        });
    }

    @Override
    public void done(Request request, DefaultResponse o) {
        String key = request.getRequestId() + request.getInterfaceName();
        String resultKey = key + "_result";
        jedisFactory.call(jedis -> {
            jedis.set(key, RequestState.DONE.name());
            jedis.set(resultKey, JSON.toJSONString(o));
            jedis.expire(key, 15 * 60);
            jedis.expire(resultKey, 15 * 60);
            return null;
        });
    }

    @Override
    public DefaultResponse getResult(Request request) {
        String key = request.getRequestId() + request.getInterfaceName() + "_result";
        String value = jedisFactory.call(jedis -> jedis.get(key));
        return value == null ? null : JSON.parseObject(value, DefaultResponse.class);
    }

    @Override
    public void clear(Request request) {
        String key = request.getRequestId() + request.getInterfaceName();
        String resultKey = key + "_result";
        jedisFactory.call(jedis -> jedis.del(key, resultKey));
    }
}
