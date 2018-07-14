package cn.huwhy.common.cache;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.alibaba.fastjson.JSON;
import cn.huwhy.common.json.JsonUtil;

/**
 * @author huwhy
 * @data 16/9/30
 * @Desc 缓存列表数据
 */
public abstract class ListCache<T, PK> extends AbstractCache<T, PK> {

    private Class<T> tClass;

    public ListCache() {
        Type type = this.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type actualType = ((ParameterizedType) type).getActualTypeArguments()[0];
            tClass = (Class<T>) actualType;
        }
    }

    public void putList(List<T> entities) {
        putString(key(), JsonUtil.toJson(entities));
    }

    public List<T> get() {
        return JSON.parseArray(getString(key()), tClass);
    }

    public String key() {
        return "list_cache:" + tClass.getName();
    }

    public void destroy() {
        jedisFactory.call(jedis -> jedis.del(key()));
    }
}
