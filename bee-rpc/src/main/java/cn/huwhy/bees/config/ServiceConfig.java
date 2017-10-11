package cn.huwhy.bees.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import cn.huwhy.bees.common.BeesConstants;
import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.config.annotation.ConfigDesc;
import cn.huwhy.bees.config.handler.ConfigHandler;
import cn.huwhy.bees.core.extension.ExtensionLoader;
import cn.huwhy.bees.exception.BeesErrorMsgConstant;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.rpc.Exporter;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.util.ConcurrentHashSet;
import cn.huwhy.bees.util.LoggerUtil;
import cn.huwhy.bees.util.NetUtils;
import cn.huwhy.bees.util.StringTools;

public class ServiceConfig<T> extends AbstractServiceConfig {

    private static final long                      serialVersionUID = -3342374271064293224L;
    private static       ConcurrentHashSet<String> existingServices = new ConcurrentHashSet<>();

    // 接口实现类引用
    private T ref;

    // service 对应的exporters，用于管理service服务的生命周期
    private List<Exporter<T>> exporters = new CopyOnWriteArrayList<>();
    private Class<T>                    interfaceClass;
    private BasicServiceInterfaceConfig basicServiceConfig;
    private AtomicBoolean          exported     = new AtomicBoolean(false);
    // service的用于注册的url，用于管理service注册的生命周期，url为registry url，内部嵌套service url。
    private ConcurrentHashSet<URL> registerUrls = new ConcurrentHashSet<>();

    public static ConcurrentHashSet<String> getExistingServices() {
        return existingServices;
    }

    public Class<?> getInterface() {
        return interfaceClass;
    }

    public void setInterface(Class<T> interfaceClass) {
        if (interfaceClass != null && !interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        this.interfaceClass = interfaceClass;
    }

    public T getRef() {
        return ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    public List<Exporter<T>> getExporters() {
        return Collections.unmodifiableList(exporters);
    }

    protected boolean serviceExists(URL url) {
        return existingServices.contains(url.getIdentity());
    }

    public synchronized void export() {
        if (exported.get()) {
            LoggerUtil.warn(String.format("%s has already been exported, so ignore the export request!", interfaceClass.getName()));
            return;
        }

        checkInterfaceAndMethods(interfaceClass);

        List<URL> registryUrls = loadRegistryUrls();
        if (registryUrls == null || registryUrls.size() == 0) {
            throw new IllegalStateException("Should set registry config for service:" + interfaceClass.getName());
        }
        doExport(export, registryUrls);

    }

    public synchronized void unExport() {
        if (!exported.get()) {
            return;
        }
        try {
            ConfigHandler configHandler =
                    ExtensionLoader.getExtensionLoader(ConfigHandler.class).getExtension(BeesConstants.DEFAULT_VALUE);
            configHandler.unExport(exporters, registerUrls);
        } finally {
            afterUnExport();
        }
    }

    @SuppressWarnings("unchecked")
    private void doExport(int port, List<URL> registryURLs) {
        String hostAddress = host;
        if (StringUtils.isBlank(hostAddress) && basicServiceConfig != null) {
            hostAddress = basicServiceConfig.getHost();
        }
        if (NetUtils.isInvalidLocalHost(hostAddress)) {
            hostAddress = getLocalHostAddress(registryURLs);
        }

        Map<String, String> map = new HashMap<>();

        map.put(URLParamType.nodeType.getName(), BeesConstants.NODE_TYPE_SERVICE);
        map.put(URLParamType.refreshTimestamp.getName(), String.valueOf(System.currentTimeMillis()));
        map.put(URLParamType.idempotent.getName(), URLParamType.idempotent.getValue());
        collectConfigParams(map, basicServiceConfig, this);

        URL serviceUrl = new URL(hostAddress, port, interfaceClass.getName(), map);

        if (serviceExists(serviceUrl)) {
            LoggerUtil.warn(String.format("%s configService is malformed, for same service (%s) already exists ", interfaceClass.getName(),
                    serviceUrl.getIdentity()));
            throw new BeesFrameworkException(String.format("%s configService is malformed, for same service (%s) already exists ",
                    interfaceClass.getName(), serviceUrl.getIdentity()), BeesErrorMsgConstant.FRAMEWORK_INIT_ERROR);
        }

        for (URL u : registryURLs) {
            u.addParameter(URLParamType.embed.getName(), StringTools.urlEncode(serviceUrl.toFullStr()));
            registerUrls.add(u.createCopy());
        }

        ConfigHandler configHandler = ExtensionLoader.getExtensionLoader(ConfigHandler.class).getExtension(BeesConstants.DEFAULT_VALUE);

        exporters.add(configHandler.export(interfaceClass, ref, registryURLs));

    }

    private void afterExport() {
        exported.set(true);
        for (Exporter<T> ep : exporters) {
            existingServices.add(ep.getProvider().getUrl().getIdentity());
        }
    }

    private void afterUnExport() {
        exported.set(false);
        for (Exporter<T> ep : exporters) {
            existingServices.remove(ep.getProvider().getUrl().getIdentity());
            exporters.remove(ep);
        }
        exporters.clear();
        registerUrls.clear();
    }

    @ConfigDesc(excluded = true)
    public BasicServiceInterfaceConfig getBasicServiceConfig() {
        return basicServiceConfig;
    }

    public void setBasicServiceConfig(BasicServiceInterfaceConfig basicServiceConfig) {
        this.basicServiceConfig = basicServiceConfig;
    }

    @ConfigDesc(excluded = true)
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public AtomicBoolean getExported() {
        return exported;
    }

    public ConcurrentHashSet<URL> getRegisterUrls() {
        return registerUrls;
    }

}
