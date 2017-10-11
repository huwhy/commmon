package cn.huwhy.bees.config.springsupport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;

import cn.huwhy.bees.config.BasicRefererInterfaceConfig;
import cn.huwhy.bees.config.BasicServiceInterfaceConfig;
import cn.huwhy.bees.config.RegistryConfig;
import cn.huwhy.bees.config.springsupport.annotation.BeesReferer;
import cn.huwhy.bees.config.springsupport.annotation.BeesService;
import cn.huwhy.bees.config.springsupport.util.SpringBeanUtil;
import cn.huwhy.bees.config.springsupport.util.SpringContentUtil;
import cn.huwhy.bees.rpc.init.Initializable;
import cn.huwhy.bees.rpc.init.InitializationFactory;
import cn.huwhy.bees.util.ConcurrentHashSet;
import cn.huwhy.bees.util.LoggerUtil;
import cn.huwhy.bees.util.StringTools;

/**
 * @author fld
 *         <p>
 *         Annotation bean for motan
 *         <p>
 *         <p>
 *         Created by fld on 16/5/13.
 */
public class AnnotationBean implements DisposableBean, BeanFactoryPostProcessor, BeanPostProcessor, BeanFactoryAware, ApplicationContextAware {

    private String id;

    private String annotationPackage;

    private String[] annotationPackages;

    private BeanFactory beanFactory;

    public AnnotationBean() {
    }

    private final Set<ServiceConfigBean<?>> serviceConfigs = new ConcurrentHashSet<ServiceConfigBean<?>>();

    private final ConcurrentMap<String, RefererConfigBean> referenceConfigs = new ConcurrentHashMap<String, RefererConfigBean>();

    static {
        //custom Initializable before motan beans inited
        Initializable initialization = InitializationFactory.getInitialization();
        initialization.init();
    }

    /**
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        if (annotationPackage == null || annotationPackage.length() == 0) {
            return;
        }
        if (beanFactory instanceof BeanDefinitionRegistry) {
            try {
                // init scanner
                Class<?> scannerClass = ClassUtils.forName("org.springframework.context.annotation.ClassPathBeanDefinitionScanner",
                        AnnotationBean.class.getClassLoader());
                Object scanner = scannerClass.getConstructor(new Class<?>[]{BeanDefinitionRegistry.class, boolean.class})
                        .newInstance(beanFactory, true);
                // add filter
                Class<?> filterClass = ClassUtils.forName("org.springframework.core.type.filter.AnnotationTypeFilter",
                        AnnotationBean.class.getClassLoader());
                Object filter = filterClass.getConstructor(Class.class).newInstance(BeesService.class);
                Method addIncludeFilter = scannerClass.getMethod("addIncludeFilter",
                        ClassUtils.forName("org.springframework.core.type.filter.TypeFilter", AnnotationBean.class.getClassLoader()));
                addIncludeFilter.invoke(scanner, filter);
                // scan packages
                Method scan = scannerClass.getMethod("scan", String[].class);
                scan.invoke(scanner, new Object[]{annotationPackages});
            } catch (Throwable e) {
                // spring 2.0
            }
        }
    }

    /**
     * init reference field
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (!isMatchPackage(bean)) {
            return bean;
        }
        Class<?> clazz = bean.getClass();
        if (isProxyBean(bean)) {
            clazz = AopUtils.getTargetClass(bean);
        }
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.length() > 3 && name.startsWith("set")
                    && method.getParameterTypes().length == 1
                    && Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers())) {
                try {
                    BeesReferer reference = method.getAnnotation(BeesReferer.class);
                    if (reference != null) {
                        Object value = refer(reference, method.getParameterTypes()[0]);
                        if (value != null) {
                            method.invoke(bean, value);
                        }
                    }
                } catch (Exception e) {
                    throw new BeanInitializationException("Failed to init remote service reference at method " + name
                            + " in class " + bean.getClass().getName(), e);
                }
            }
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                BeesReferer reference = field.getAnnotation(BeesReferer.class);
                if (reference != null) {
                    Object value = refer(reference, field.getType());
                    if (value != null) {
                        field.set(bean, value);
                    }
                }
            } catch (Exception e) {
                throw new BeanInitializationException("Failed to init remote service reference at filed " + field.getName()
                        + " in class " + bean.getClass().getName(), e);
            }
        }
        return bean;
    }

    /**
     * init service config and export servcice
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!isMatchPackage(bean)) {
            return bean;
        }
        Class<?> clazz = bean.getClass();
        if (isProxyBean(bean)) {
            clazz = AopUtils.getTargetClass(bean);
        }
        BeesService service = clazz.getAnnotation(BeesService.class);
        if (service != null) {
            ServiceConfigBean<Object> serviceConfig = new ServiceConfigBean<>();
            if (void.class.equals(service.interfaceClass())) {
                if (clazz.getInterfaces().length > 0) {
                    Class<Object> clz = (Class<Object>) clazz.getInterfaces()[0];
                    serviceConfig.setInterface(clz);
                } else {
                    throw new IllegalStateException("Failed to export remote service class " + clazz.getName()
                            + ", cause: The @Service undefined interfaceClass or interfaceName, and the service class unimplemented any interfaces.");
                }
            } else {
                serviceConfig.setInterface((Class<Object>) service.interfaceClass());
            }
            if (beanFactory != null) {
                serviceConfig.setBeanFactory(beanFactory);

                if (StringTools.notBlank(service.basicService())) {
                    serviceConfig.setBasicServiceConfig(beanFactory.getBean(service.basicService(),
                            BasicServiceInterfaceConfig.class));
                }
                if (StringTools.notBlank(service.group())) {
                    serviceConfig.setGroup(service.group());
                }

                if (StringTools.notBlank(service.version())) {
                    serviceConfig.setVersion(service.version());
                }

                if (StringTools.notBlank(service.proxy())) {
                    serviceConfig.setProxy(service.proxy());
                }

                if (StringTools.notBlank(service.filter())) {
                    serviceConfig.setFilter(service.filter());
                }

                if (service.actives() > 0) {
                    serviceConfig.setActives(service.actives());
                }

                if (service.async()) {
                    serviceConfig.setAsync(service.async());
                }

                // 是否共享 channel
                if (service.shareChannel()) {
                    serviceConfig.setShareChannel(service.shareChannel());
                }

                // if throw exception when call failure，the default value is ture
                if (service.throwException()) {
                    serviceConfig.setThrowException(service.throwException());
                }
                if (service.requestTimeout() > 0) {
                    serviceConfig.setRequestTimeout(service.requestTimeout());
                }

                if (service.accessLog()) {
                    serviceConfig.setAccessLog("true");
                }
                if (service.check()) {
                    serviceConfig.setCheck("true");
                }
                if (service.useGz()) {
                    serviceConfig.setUsegz(service.useGz());
                }

                if (service.retries() > 0) {
                    serviceConfig.setRetries(service.retries());
                }

                if (service.minGzSize() > 0) {
                    serviceConfig.setMingzSize(service.minGzSize());
                }

                if (StringTools.notBlank(service.codec())) {
                    serviceConfig.setCodec(service.codec());
                }

                try {
                    serviceConfig.afterPropertiesSet();
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
            serviceConfig.setRef(bean);
            serviceConfigs.add(serviceConfig);
            serviceConfig.export();
        }
        return bean;
    }

    /**
     * release service/reference
     *
     * @throws Exception
     */
    public void destroy() throws Exception {
        for (ServiceConfigBean<?> serviceConfig : serviceConfigs) {
            try {
                serviceConfig.unExport();
            } catch (Throwable e) {
                LoggerUtil.error(e.getMessage(), e);
            }
        }
        for (RefererConfigBean<?> referenceConfig : referenceConfigs.values()) {
            try {
                referenceConfig.destroy();
            } catch (Throwable e) {
                LoggerUtil.error(e.getMessage(), e);
            }
        }
    }

    /**
     * refer proxy
     *
     * @param reference
     * @param referenceClass
     * @param <T>
     * @return
     */
    private <T> Object refer(BeesReferer reference, Class<?> referenceClass) {
        String interfaceName;
        if (!void.class.equals(reference.interfaceClass())) {
            interfaceName = reference.interfaceClass().getName();
        } else if (referenceClass.isInterface()) {
            interfaceName = referenceClass.getName();
        } else {
            throw new IllegalStateException("The @Reference undefined interfaceClass or interfaceName, and the property type "
                    + referenceClass.getName() + " is not a interface.");
        }
        String key = reference.group() + "/" + interfaceName + ":" + reference.version();
        RefererConfigBean<T> referenceConfig = referenceConfigs.get(key);
        if (referenceConfig == null) {
            referenceConfig = new RefererConfigBean<T>();
            referenceConfig.setBeanFactory(beanFactory);
            if (void.class.equals(reference.interfaceClass())
                    && referenceClass.isInterface()) {
                referenceConfig.setInterface((Class<T>) referenceClass);
            } else if (!void.class.equals(reference.interfaceClass())) {
                referenceConfig.setInterface((Class<T>) reference.interfaceClass());
            }

            if (beanFactory != null) {

                if (StringTools.notBlank(reference.directUrl())) {
                    referenceConfig.setDirectUrl(reference.directUrl());
                }

                if (StringTools.notBlank(reference.basicReferer())) {
                    BasicRefererInterfaceConfig biConfig = beanFactory.getBean(reference.basicReferer(), BasicRefererInterfaceConfig.class);
                    if (biConfig != null) {
                        referenceConfig.setBasicReferer(biConfig);
                    }
                }

                if (StringTools.notBlank(reference.registry())) {
                    List<RegistryConfig> registryConfigs = SpringBeanUtil.getMultiBeans(beanFactory, reference
                            .registry(), SpringBeanUtil.COMMA_SPLIT_PATTERN, RegistryConfig.class);
                    referenceConfig.setRegistries(registryConfigs);
                }

                if (StringTools.notBlank(reference.application())) {
                    referenceConfig.setApplication(reference.application());
                }
                if (StringTools.notBlank(reference.module())) {
                    referenceConfig.setModule(reference.module());
                }
                if (StringTools.notBlank(reference.group())) {
                    referenceConfig.setGroup(reference.group());
                }

                if (StringTools.notBlank(reference.version())) {
                    referenceConfig.setVersion(reference.version());
                }

                if (StringTools.notBlank(reference.proxy())) {
                    referenceConfig.setProxy(reference.proxy());
                }

                if (StringTools.notBlank(reference.filter())) {
                    referenceConfig.setFilter(reference.filter());
                }

                if (reference.actives() > 0) {
                    referenceConfig.setActives(reference.actives());
                }

                if (reference.async()) {
                    referenceConfig.setAsync(reference.async());
                }

                if (reference.shareChannel()) {
                    referenceConfig.setShareChannel(reference.shareChannel());
                }

                // if throw exception when call failure，the default value is ture
                if (reference.throwException()) {
                    referenceConfig.setThrowException(reference.throwException());
                }
                if (reference.requestTimeout() > 0) {
                    referenceConfig.setRequestTimeout(reference.requestTimeout());
                }
                if (reference.register()) {
                    referenceConfig.setRegister(reference.register());
                }
                if (reference.accessLog()) {
                    referenceConfig.setAccessLog("true");
                }
                if (reference.check()) {
                    referenceConfig.setCheck("true");
                }
                if (reference.retries() > 0) {
                    referenceConfig.setRetries(reference.retries());
                }
                if (reference.usegz()) {
                    referenceConfig.setUsegz(reference.usegz());
                }
                if (reference.mingzSize() > 0) {
                    referenceConfig.setMingzSize(reference.mingzSize());
                }
                if (reference.codec() != null && reference.codec().length() > 0) {
                    referenceConfig.setCodec(reference.codec());
                }

                try {
                    referenceConfig.afterPropertiesSet();
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
            referenceConfigs.putIfAbsent(key, referenceConfig);
            referenceConfig = referenceConfigs.get(key);
        }

        return referenceConfig.getRef();
    }

    private boolean isMatchPackage(Object bean) {
        if (annotationPackages == null || annotationPackages.length == 0) {
            return true;
        }
        Class clazz = bean.getClass();
        if (isProxyBean(bean)) {
            clazz = AopUtils.getTargetClass(bean);
        }
        String beanClassName = clazz.getName();
        for (String pkg : annotationPackages) {
            if (beanClassName.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean isProxyBean(Object bean) {
        return AopUtils.isAopProxy(bean);
    }

    public String getPackage() {
        return annotationPackage;
    }

    public void setPackage(String annotationPackage) {
        this.annotationPackage = annotationPackage;
        this.annotationPackages = (annotationPackage == null || annotationPackage.length() == 0) ? null
                : annotationPackage.split(SpringBeanUtil.COMMA_SPLIT_PATTERN);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContentUtil.setApplicationContext(applicationContext);
    }
}
