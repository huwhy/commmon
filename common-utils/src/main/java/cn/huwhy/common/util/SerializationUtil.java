package cn.huwhy.common.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializationUtil {

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    /**
     * 基本数据类型
     */
    private static List<Class<?>> primitiveClasses = new ArrayList<>();

    static {
        primitiveClasses.add(boolean.class);
        primitiveClasses.add(Boolean.class);

        primitiveClasses.add(char.class);
        primitiveClasses.add(Character.class);

        primitiveClasses.add(byte.class);
        primitiveClasses.add(Byte.class);

        primitiveClasses.add(short.class);
        primitiveClasses.add(Short.class);

        primitiveClasses.add(int.class);
        primitiveClasses.add(Integer.class);

        primitiveClasses.add(long.class);
        primitiveClasses.add(Long.class);

        primitiveClasses.add(float.class);
        primitiveClasses.add(Float.class);

        primitiveClasses.add(double.class);
        primitiveClasses.add(Double.class);

        primitiveClasses.add(BigInteger.class);
        primitiveClasses.add(BigDecimal.class);

        primitiveClasses.add(String.class);
        primitiveClasses.add(java.util.Date.class);
    }

    public static boolean isSupport(Class<?> clazz) {
        return primitiveClasses.contains(clazz);
    }

    public static <T> byte[] serialize(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        if (isSupport(cls)) {
            return toStr(obj).getBytes();
        }
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        Schema<T> schema = getSchema(cls);
        return ProtobufIOUtil.toByteArray(obj, schema, buffer);
    }

    public static <T> T unserialize(byte[] data, Class<T> cls) {
        if (data == null || data.length == 0) {
            return null;
        }
        if (isSupport(cls)) {
            return toObj(new String(data), cls);
        }
        try {
            T message = cls.newInstance();
            Schema<T> schema = getSchema(cls);
            ProtobufIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static <T> List<T> unserialize(List<byte[]> data, Class<T> cls) {
        List<T> result = new ArrayList<>(data.size());
        for (byte[] bytes : data) {
            result.add(unserialize(bytes, cls));
        }
        return result;
    }

    private static String toStr(Object obj) {
        if (obj == null) {
            return "";
        } else if (obj.getClass() == Date.class) {
            return Long.toString(((Date) obj).getTime());
        } else {
            return obj.toString();
        }
    }

    private static <T> T toObj(String value, Class<T> cls) {
        if (value == null) {
            return null;
        }

        if (cls == null) {
            throw new IllegalArgumentException("arg Class[cls] is null");
        }
        if (cls == String.class) {
            return (T) value;
        }

        if (cls.isAssignableFrom(value.getClass())) {
            return (T) value;
        }

        if (cls == boolean.class || cls == Boolean.class) {
            return (T) Boolean.valueOf(value);
        }

        if (cls == byte.class || cls == Byte.class) {
            if ("".equals(value)) {
                return null;
            } else {
                return (T) Byte.valueOf(value);
            }
        }

        if (cls == short.class || cls == Short.class) {
            return (T) Short.valueOf(value);
        }

        if (cls == int.class || cls == Integer.class) {
            return (T) Integer.valueOf(value);
        }

        if (cls == long.class || cls == Long.class) {
            return (T) Long.valueOf(value);
        }

        if (cls == float.class || cls == Float.class) {
            return (T) Float.valueOf(value);
        }

        if (cls == double.class || cls == Double.class) {
            return (T) Double.valueOf(value);
        }

        if (cls == BigInteger.class) {
            return (T) new BigInteger(value);
        }

        if (cls == BigDecimal.class) {
            return (T) new BigInteger(value);
        }

        if (cls == Date.class) {
            return (T) new Date(Long.parseLong(value));
        }
        throw new ClassCastException("can not cast to :" + cls.getName());
    }

    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema != null) {
            schema = RuntimeSchema.createFrom(cls);
            cachedSchema.put(cls, schema);
        }
        return schema;
    }
}
