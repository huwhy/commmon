package cn.huwhy.common.json;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import static com.alibaba.fastjson.serializer.SerializerFeature.NotWriteDefaultValue;
import static com.alibaba.fastjson.serializer.SerializerFeature.SkipTransientField;

/**
 * @author huwhy
 * @date 2016/12/19
 * @Desc
 */
public class JsonUtil {

    public static final SerializerFeature[] FEATURES = new SerializerFeature[]{
            SkipTransientField, NotWriteDefaultValue
    };

    public static <T> String toJson(T o) {
        return JSON.toJSONString(o, FEATURES);
    }

    public static <T> T toObject(String json, Class<T> tClass) {
        return JSON.parseObject(json, tClass);
    }

    public static <T> List<T> toList(String json, Class<T> tClass) {
        return JSON.parseArray(json, tClass);
    }

}
