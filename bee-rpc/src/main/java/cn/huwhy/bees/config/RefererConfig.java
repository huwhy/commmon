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

package cn.huwhy.bees.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import cn.huwhy.bees.cluster.Cluster;
import cn.huwhy.bees.cluster.support.ClusterSupport;
import cn.huwhy.bees.common.BeesConstants;
import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.config.annotation.ConfigDesc;
import cn.huwhy.bees.config.handler.ConfigHandler;
import cn.huwhy.bees.core.extension.ExtensionLoader;
import cn.huwhy.bees.exception.BeesErrorMsgConstant;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.registry.RegistryService;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.util.NetUtils;
import cn.huwhy.bees.util.StringTools;

/**
 * Referer config.
 *
 * @author fishermen
 * @version V1.0 created at: 2013-5-17
 */

public class RefererConfig<T> extends AbstractRefererConfig {

    private static final long serialVersionUID = -2299754608229467887L;

    private Class<T> interfaceClass;

    // 点对点直连服务提供地址
    private String directUrl;

    private AtomicBoolean initialized = new AtomicBoolean(false);

    private T ref;

    private BasicRefererInterfaceConfig basicReferer;

    private ClusterSupport<T> clusterSupport;

    public T getRef() {
        if (ref == null) {
            initRef();
        }
        return ref;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public synchronized void initRef() {
        if (initialized.get()) {
            return;
        }

        try {
            interfaceClass = (Class) Class.forName(interfaceClass.getName(), true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new BeesFrameworkException("ReferereConfig initRef Error: Class not found " + interfaceClass.getName(), e,
                    BeesErrorMsgConstant.FRAMEWORK_INIT_ERROR);
        }

        checkInterfaceAndMethods(interfaceClass);

        List<Cluster<T>> clusters = new ArrayList<>();

        ConfigHandler configHandler = ExtensionLoader.getExtensionLoader(ConfigHandler.class).getExtension(BeesConstants.DEFAULT_VALUE);

        List<URL> registryUrls = loadRegistryUrls();
        String localIp = getLocalHostAddress(registryUrls);
        Map<String, String> params = new HashMap<>();
        params.put(URLParamType.nodeType.getName(), BeesConstants.NODE_TYPE_REFERER);
        params.put(URLParamType.version.getName(), URLParamType.version.getValue());
        params.put(URLParamType.refreshTimestamp.getName(), String.valueOf(System.currentTimeMillis()));

        collectConfigParams(params, basicReferer, this);

        URL refUrl = new URL(localIp, BeesConstants.DEFAULT_INT_VALUE, interfaceClass.getName(), params);
        clusterSupport = createClusterSupport(refUrl, configHandler, registryUrls);
        clusters.add(clusterSupport.getCluster());
        proxy = refUrl.getParameter(URLParamType.proxy.getName(), URLParamType.proxy.getValue());
        ref = configHandler.refer(interfaceClass, clusters, proxy);

        initialized.set(true);
    }

    private ClusterSupport<T> createClusterSupport(URL refUrl, ConfigHandler configHandler, List<URL> registryUrls) {
        List<URL> regUrls = new ArrayList<URL>();

        // 如果用户指定directUrls 或者 injvm协议访问，则使用local registry
        if (StringUtils.isNotBlank(directUrl)) {
            URL regUrl = new URL(NetUtils.LOCALHOST, BeesConstants.DEFAULT_INT_VALUE,
                    RegistryService.class.getName());
            if (StringUtils.isNotBlank(directUrl)) {
                StringBuilder duBuf = new StringBuilder(128);
                String[] dus = BeesConstants.COMMA_SPLIT_PATTERN.split(directUrl);
                for (String du : dus) {
                    if (du.contains(":")) {
                        String[] hostPort = du.split(":");
                        URL durl = refUrl.createCopy();
                        durl.setHost(hostPort[0].trim());
                        durl.setPort(Integer.parseInt(hostPort[1].trim()));
                        durl.addParameter(URLParamType.nodeType.getName(), BeesConstants.NODE_TYPE_SERVICE);
                        duBuf.append(StringTools.urlEncode(durl.toFullStr())).append(BeesConstants.COMMA_SEPARATOR);
                    }
                }
                if (duBuf.length() > 0) {
                    duBuf.deleteCharAt(duBuf.length() - 1);
                    regUrl.addParameter(URLParamType.directUrl.getName(), duBuf.toString());
                }
            }
            regUrls.add(regUrl);
        } else { // 通过注册中心配置拼装URL，注册中心可能在本地，也可能在远端
            if (registryUrls == null || registryUrls.isEmpty()) {
                throw new IllegalStateException(
                        String.format(
                                "No registry to reference %s on the consumer %s , please config <motan:registry address=\"...\" /> in your spring config.",
                                interfaceClass, NetUtils.LOCALHOST));
            }
            for (URL url : registryUrls) {
                regUrls.add(url.createCopy());
            }
        }

        for (URL url : regUrls) {
            url.addParameter(URLParamType.embed.getName(), StringTools.urlEncode(refUrl.toFullStr()));
        }
        return configHandler.buildClusterSupport(interfaceClass, regUrls);
    }

    public synchronized void destroy() {
        if (clusterSupport != null) {
            clusterSupport.destroy();
        }
        ref = null;
        initialized.set(false);
    }

    public void setInterface(Class<T> interfaceClass) {
        if (interfaceClass != null && !interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        this.interfaceClass = interfaceClass;
    }

    public Class<?> getInterface() {
        return interfaceClass;
    }

    public String getDirectUrl() {
        return directUrl;
    }

    public void setDirectUrl(String directUrl) {
        this.directUrl = directUrl;
    }

    @ConfigDesc(excluded = true)
    public BasicRefererInterfaceConfig getBasicReferer() {
        return basicReferer;
    }

    public void setBasicReferer(BasicRefererInterfaceConfig basicReferer) {
        this.basicReferer = basicReferer;
    }

    public ClusterSupport<T> getClusterSupport() {
        return clusterSupport;
    }

    public AtomicBoolean getInitialized() {
        return initialized;
    }

}
