package cn.huwhy.common.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.beanutils.BeanUtils.copyProperty;

public class BeanCopyUtils {
    private static final Logger logger = LoggerFactory.getLogger(BeanCopyUtils.class);

    public static abstract class CopyFunction<T, V> {
        private String name;

        public CopyFunction(String name) {
            this.name = name;
        }

        public abstract V copy(T t);

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static Map<String, CopyFunction> of(CopyFunction... functions) {
        Map<String, CopyFunction> map = new HashMap<>();
        for (CopyFunction func : functions) {
            map.put(func.getName(), func);
        }
        return map;
    }

    public static <T, K> List<K> copyProperties(List<T> origins, Class<K> clazz, Map<String, CopyFunction> copyFunctions,
                                                String... ignoreProperties) {
        List<K> descList = new ArrayList<>();
        if (origins == null || origins.isEmpty())
            return descList;
        for (T origin : origins) {
            descList.add(copyProperties(origin, clazz, copyFunctions, ignoreProperties));
        }
        return descList;
    }

    public static <T, K> List<K> copyProperties(List<T> origins, Class<K> clazz, String... ignoreProperties) {
        List<K> descList = new ArrayList<>();
        if (origins == null || origins.isEmpty())
            return descList;
        for (T origin : origins) {
            descList.add(copyProperties(origin, clazz, ignoreProperties));
        }
        return descList;
    }

    public static <K> K copyProperties(Object origin, Class<K> clazz, String... ignoreProperties) {
        if (origin == null)
            return null;
        try {
            K desc = clazz.newInstance();
            copyProperties(origin, desc, ignoreProperties);
            return desc;
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("error in new instance " + clazz, e);
            return null;
        }
    }

    public static <K> K copyProperties(Object origin, Class<K> clazz, Map<String, CopyFunction> copyFunctions,
                                       String... ignoreProperties) {
        if (origin == null)
            return null;
        try {
            K desc = clazz.newInstance();
            copyProperties(origin, desc, copyFunctions, ignoreProperties);
            return desc;
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("error in new instance " + clazz, e);
            return null;
        }
    }

    public static void copyProperties(Object orig, Object dest, String... ignoreProperties) {
        copyProperties(orig, dest, new HashMap<>(), ignoreProperties);
    }

    public static void copyProperties(Object orig, Object dest, Map<String, CopyFunction> copyFunctions,
                                      String... ignoreProperties) {
        // Validate existence of the specified beans
        if (dest == null || orig == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("BeanUtils.copyProperties(" + dest + ", " +
                    orig + ")");
        }

        Set<String> ignoreSet = new HashSet<>();
        if (ignoreProperties != null && ignoreProperties.length > 0) {
            ignoreSet.addAll(Arrays.asList(ignoreProperties));
        }

        try {
            // Copy the properties, converting as necessary
            if (orig instanceof DynaBean) {
                DynaProperty[] origDescriptors =
                        ((DynaBean) orig).getDynaClass().getDynaProperties();
                for (int i = 0; i < origDescriptors.length; i++) {
                    String name = origDescriptors[i].getName();
                    if (!ignoreSet.contains(name)) {
                        // Need to check isReadable() for WrapDynaBean
                        // (see Jira issue# BEANUTILS-61)
                        if (getPropertyUtils().isReadable(orig, name) &&
                                BeanUtilsBean.getInstance().getPropertyUtils().isWriteable(dest, name)) {
                            Object value = ((DynaBean) orig).get(name);
                            if (value != null) {
                                CopyFunction func = copyFunctions.get(name);
                                if (func != null) {
                                    value = func.copy(value);
                                }
                                BeanUtilsBean.getInstance().copyProperty(dest, name, value);
                            }
                        }
                    }
                }
            } else if (orig instanceof Map) {
                Iterator entries = ((Map) orig).entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry) entries.next();
                    String name = (String) entry.getKey();
                    if (!ignoreSet.contains(name)) {
                        if (getPropertyUtils().isWriteable(dest, name)) {
                            Object value = entry.getValue();
                            if (value != null) {
                                CopyFunction func = copyFunctions.get(name);
                                if (func != null) {
                                    value = func.copy(value);
                                }
                                copyProperty(dest, name, value);
                            }
                        }
                    }
                }
            } else /* if (orig is a standard JavaBean) */ {
                PropertyDescriptor[] origDescriptors =
                        getPropertyUtils().getPropertyDescriptors(orig);
                for (int i = 0; i < origDescriptors.length; i++) {
                    String name = origDescriptors[i].getName();
                    if ("class".equals(name)) {
                        continue; // No point in trying to set an object's class
                    }
                    if (!ignoreSet.contains(name)) {
                        if (getPropertyUtils().isReadable(orig, name) &&
                                getPropertyUtils().isWriteable(dest, name)) {
                            try {
                                Object value =
                                        getPropertyUtils().getSimpleProperty(orig, name);
                                if (value != null) {
                                    CopyFunction func = copyFunctions.get(name);
                                    if (func != null) {
                                        value = func.copy(value);
                                    }
                                    copyProperty(dest, name, value);
                                }
                            } catch (NoSuchMethodException e) {
                                // Should not happen
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("error in copy properties", e);
        }
    }

    private static PropertyUtilsBean getPropertyUtils() {
        return BeanUtilsBean.getInstance().getPropertyUtils();
    }
}
