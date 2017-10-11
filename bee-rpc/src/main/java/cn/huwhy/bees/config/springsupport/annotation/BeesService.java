package cn.huwhy.bees.config.springsupport.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
public @interface BeesService {

    Class<?> interfaceClass() default void.class;

    String basicService() default "";

    String export() default "";

    // 注册中心的配置列表
    String registry() default "";

    // 分组
    String group() default "";

    // 服务版本
    String version() default "";

    // 代理类型
    String proxy() default "";

    // 过滤器
    String filter() default "";

    // 最大并发调用
    int actives() default 0;

    // 是否异步
    boolean async() default false;

    // 是否共享 channel
    boolean shareChannel() default false;

    // if throw exception when call failure，the default value is ture
    boolean throwException() default false;

    // 请求超时时间
    int requestTimeout() default 0;

    // 是否注册
    boolean register() default false;

    // 是否记录访问日志，true记录，false不记录
    boolean accessLog() default false;

    // 是否进行check，如果为true，则在监测失败后抛异常
    boolean check() default false;

    // 重试次数
    int retries() default 0;

    // 是否开启gzip压缩
    boolean useGz() default false;

    // 进行gzip压缩的最小阈值，usegz开启，且大于此值时才进行gzip压缩。单位Byte
    int minGzSize() default 0;

    String codec() default "";
}
