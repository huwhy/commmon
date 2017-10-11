package cn.huwhy.bees.config.springsupport.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * @author huwhy
 * @data 2016/11/25
 * @Desc
 */
public class SpringContentUtil {

    private static ApplicationContext ctx;

    public static void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

    public static boolean isEnable() {
        return ctx != null;
    }

    public static <T> T getBean(String name, Class<T> cls) {
        return ctx.getBean(name, cls);
    }

    public static <T> T getBean(Class<T> cls) {
        return ctx.getBean(cls);
    }
}
