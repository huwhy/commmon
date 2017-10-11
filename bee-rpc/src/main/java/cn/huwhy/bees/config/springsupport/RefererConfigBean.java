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
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;

import cn.huwhy.bees.config.BasicRefererInterfaceConfig;
import cn.huwhy.bees.config.RefererConfig;
import cn.huwhy.bees.config.RegistryConfig;
import cn.huwhy.bees.util.CollectionUtil;
import cn.huwhy.bees.util.BeesFrameworkUtil;

public class RefererConfigBean<T> extends RefererConfig<T> implements FactoryBean<T>, BeanFactoryAware, InitializingBean, DisposableBean {

    private static final long serialVersionUID = 8381310907161365567L;

    private transient BeanFactory beanFactory;

    @Override
    public T getObject() throws Exception {
        return getRef();
    }

    @Override
    public Class<?> getObjectType() {
        return getInterface();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // basicConfig需要首先配置，因为其他可能会依赖于basicConfig的配置

        checkAndConfigBasicConfig();
        checkAndConfigRegistry();

    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * 检查并配置basicConfig
     */
    private void checkAndConfigBasicConfig() {
        if (getBasicReferer() == null) {
            //            if (MotanNamespaceHandler.basicRefererConfigDefineNames.size() == 0) {
            if (beanFactory instanceof ListableBeanFactory) {
                ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;
                String[] basicRefererConfigNames = listableBeanFactory.getBeanNamesForType
                        (BasicRefererInterfaceConfig
                                .class);
                for (String name : basicRefererConfigNames) {
                    BasicRefererInterfaceConfig biConfig = beanFactory.getBean(name, BasicRefererInterfaceConfig.class);
                    if (biConfig == null) {
                        continue;
                    }
                    if (basicRefererConfigNames.length == 1) {
                        setBasicReferer(biConfig);
                    } else if (biConfig.isDefault() != null && biConfig.isDefault().booleanValue()) {
                        setBasicReferer(biConfig);
                    }
                }
                //                    MotanNamespaceHandler.basicRefererConfigDefineNames.addAll(Arrays.asList(basicRefererConfigNames));
            }
            //            }

        }
    }

    /**
     * 检查并配置registry
     */
    public void checkAndConfigRegistry() {
        if (CollectionUtil.isEmpty(getRegistries()) && getBasicReferer() != null
                && !CollectionUtil.isEmpty(getBasicReferer().getRegistries())) {
            setRegistries(getBasicReferer().getRegistries());
        }
        if (CollectionUtil.isEmpty(getRegistries())) {
            RegistryConfig rc = beanFactory.getBean(RegistryConfig.class);
            setRegistry(rc);
        }
        if (CollectionUtil.isEmpty(getRegistries())) {
            setRegistry(BeesFrameworkUtil.getDefaultRegistryConfig());
        }
    }
}
