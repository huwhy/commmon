package cn.huwhy.bees.config.springsupport;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

import cn.huwhy.bees.config.BasicServiceInterfaceConfig;
import cn.huwhy.bees.config.RegistryConfig;
import cn.huwhy.bees.config.springsupport.util.SpringBeanUtil;

public class BasicServiceConfigBean extends BasicServiceInterfaceConfig implements BeanNameAware,
        InitializingBean, BeanFactoryAware {

    private String registryNames;


    BeanFactory beanFactory;

    @Override
    public void setBeanName(String name) {
        setId(name);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        setRegistries(extractRegistries(registryNames, beanFactory));
    }


    public List<RegistryConfig> extractRegistries(String registries, BeanFactory beanFactory) {
        if (registries != null && registries.length() > 0) {
            if (!registries.contains(",")) {
                RegistryConfig registryConfig = beanFactory.getBean(registries, RegistryConfig.class);
                return Collections.singletonList(registryConfig);
            } else {
                List<RegistryConfig> registryConfigList = SpringBeanUtil.getMultiBeans(beanFactory, registries,
                        SpringBeanUtil.COMMA_SPLIT_PATTERN, RegistryConfig.class);
                return registryConfigList;
            }
        } else {
            return null;
        }
    }


    public void setRegistry(String registryNames) {
        this.registryNames = registryNames;
    }

    public void setCheck(boolean value) {
        setCheck(String.valueOf(value));
    }

    public void setAccessLog(boolean value) {
        setAccessLog(String.valueOf(value));
    }
}
