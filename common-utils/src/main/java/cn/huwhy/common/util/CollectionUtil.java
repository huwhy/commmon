package cn.huwhy.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class CollectionUtil {
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return (null == collection || collection.isEmpty());
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static <K, V> Map<K, V> index(Collection<V> collection, Function<? super V, K> keyFunction) {
        if (isEmpty(collection))
            return new HashMap<>();
        return Maps.uniqueIndex(new HashSet<V>(collection), keyFunction::apply);
    }
}
