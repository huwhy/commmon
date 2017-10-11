/*
 *  Copyright 2009-2016 Weibo, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package cn.huwhy.bees.config.springsupport;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import cn.huwhy.bees.config.BasicServiceInterfaceConfig;
import cn.huwhy.bees.config.RegistryConfig;
import cn.huwhy.bees.config.ServiceConfig;
import cn.huwhy.bees.exception.BeesErrorMsgConstant;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.util.CollectionUtil;
import cn.huwhy.bees.util.BeesFrameworkUtil;

public class ServiceConfigBean<T> extends ServiceConfig<T>
        implements
        BeanPostProcessor,
        BeanFactoryAware,
        InitializingBean,
        DisposableBean,
        ApplicationListener<ContextRefreshedEvent> {

    private static final long serialVersionUID = -7247592395983804440L;

    private transient BeanFactory beanFactory;

    @Override
    public void destroy() throws Exception {
        unExport();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // 注意:basicConfig需要首先配置，因为其他可能会依赖于basicConfig的配置
        checkAndConfigBasicConfig();
        checkAndConfigExport();
        checkAndConfigRegistry();

        // 等spring初始化完毕后，再export服务
        // export();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    // 为了让serviceBean最早加载
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!getExported().get()) {
            export();
        }
    }

    /**
     * 检查并配置basicConfig
     */
    private void checkAndConfigBasicConfig() {
        if (getBasicServiceConfig() == null) {
            if (beanFactory instanceof ListableBeanFactory) {
                ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;
                String[] basicServiceConfigNames = listableBeanFactory.getBeanNamesForType
                        (BasicServiceInterfaceConfig
                                .class);
                for (String name : basicServiceConfigNames) {
                    BasicServiceInterfaceConfig biConfig = beanFactory.getBean(name, BasicServiceInterfaceConfig.class);
                    if (biConfig == null) {
                        continue;
                    }
                    if (basicServiceConfigNames.length == 1) {
                        setBasicServiceConfig(biConfig);
                    } else if (biConfig.isDefault() != null && biConfig.isDefault().booleanValue()) {
                        setBasicServiceConfig(biConfig);
                    }
                }
            }

        }
    }

    /**
     * 检查是否已经装配export，如果没有则到basicConfig查找
     */
    private void checkAndConfigExport() {
        if (getExport() == null && getBasicServiceConfig() != null
                && getBasicServiceConfig().getExport() > 0) {
            setExport(getBasicServiceConfig().getExport());
        }

        if (getExport() == 0) {
            throw new BeesFrameworkException(String.format("%s ServiceConfig must config right export value!", getInterface().getName()),
                    BeesErrorMsgConstant.FRAMEWORK_INIT_ERROR);
        }
    }

    /**
     * 检查并配置registry
     */
    private void checkAndConfigRegistry() {
        if (CollectionUtil.isEmpty(getRegistries()) && getBasicServiceConfig() != null
                && !CollectionUtil.isEmpty(getBasicServiceConfig().getRegistries())) {
            setRegistries(getBasicServiceConfig().getRegistries());
        }
        if (CollectionUtil.isEmpty(getRegistries())) {
            for (RegistryConfig rc : getRegistries()) {
                if (getRegistries().size() == 1) {
                    setRegistry(rc);
                } else if (rc.isDefault() != null && rc.isDefault()) {
                    setRegistry(rc);
                }
            }
        }
        if (CollectionUtil.isEmpty(getRegistries())) {
            setRegistry(BeesFrameworkUtil.getDefaultRegistryConfig());
        }
    }

}
