package cn.huwhy.bees.config.handler;

import java.util.Collection;
import java.util.List;

import cn.huwhy.bees.cluster.Cluster;
import cn.huwhy.bees.cluster.support.ClusterSupport;
import cn.huwhy.bees.common.BeesConstants;
import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.core.extension.ExtensionLoader;
import cn.huwhy.bees.core.extension.SpiMeta;
import cn.huwhy.bees.exception.BeesErrorMsg;
import cn.huwhy.bees.exception.BeesErrorMsgConstant;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.proxy.ProxyFactory;
import cn.huwhy.bees.proxy.RefererInvocationHandler;
import cn.huwhy.bees.registry.Registry;
import cn.huwhy.bees.registry.RegistryFactory;
import cn.huwhy.bees.rpc.DefaultProvider;
import cn.huwhy.bees.rpc.DefaultRpcExporter;
import cn.huwhy.bees.rpc.Exporter;
import cn.huwhy.bees.rpc.Provider;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.util.LoggerUtil;
import cn.huwhy.bees.util.StringTools;

@SpiMeta(name = BeesConstants.DEFAULT_VALUE)
public class SimpleConfigHandler implements ConfigHandler {

    @Override
    public <T> ClusterSupport<T> buildClusterSupport(Class<T> interfaceClass, List<URL> registryUrls) {
        ClusterSupport<T> clusterSupport = new ClusterSupport<>(interfaceClass, registryUrls);
        clusterSupport.init();

        return clusterSupport;
    }

    @Override
    public <T> T refer(Class<T> interfaceClass, List<Cluster<T>> clusters, String proxyType) {
        ProxyFactory proxyFactory = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getExtension(proxyType);
        return proxyFactory.getProxy(interfaceClass, new RefererInvocationHandler<>(interfaceClass, clusters));
    }

    @Override
    public <T> Exporter<T> export(Class<T> interfaceClass, T ref, List<URL> registryUrls) {

        String serviceStr = StringTools.urlDecode(registryUrls.get(0).getParameter(URLParamType.embed.getName()));
        URL serviceUrl = URL.valueOf(serviceStr);

        // export service
        Provider<T> provider = new DefaultProvider<>(ref, serviceUrl, interfaceClass);
        Exporter<T> exporter = new DefaultRpcExporter<>(provider, serviceUrl);
        exporter.init();
        // register service
        register(registryUrls, serviceUrl);

        return exporter;
    }

    @Override
    public <T> void unExport(List<Exporter<T>> exporters, Collection<URL> registryUrls) {
        try {
            unRegister(registryUrls);
        } catch (Exception e1) {
            LoggerUtil.warn("Exception when unregister urls:" + registryUrls);
        }
        try {
            for (Exporter<T> exporter : exporters) {
                exporter.unExport();
            }
        } catch (Exception e) {
            LoggerUtil.warn("Exception when unExport exporters:" + exporters);
        }
    }

    private void register(List<URL> registryUrls, URL serviceUrl) {

        for (URL url : registryUrls) {
            // 根据check参数的设置，register失败可能会抛异常，上层应该知晓
            RegistryFactory registryFactory = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getExtension("zookeeper");
            if (registryFactory == null) {
                throw new BeesFrameworkException(new BeesErrorMsg(500, BeesErrorMsgConstant.FRAMEWORK_REGISTER_ERROR_CODE,
                        "register error! Could not find extension for registry, make sure registry module for is in classpath!"));
            }
            Registry registry = registryFactory.getRegistry(url);
            registry.register(serviceUrl);
        }
    }

    private void unRegister(Collection<URL> registryUrls) {
        for (URL url : registryUrls) {
            // 不管check的设置如何，做完所有unregistry，做好清理工作
            try {
                String serviceStr = StringTools.urlDecode(url.getParameter(URLParamType.embed.getName()));
                URL serviceUrl = URL.valueOf(serviceStr);

                RegistryFactory registryFactory = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getExtension("zookeeper");
                Registry registry = registryFactory.getRegistry(url);
                registry.unregister(serviceUrl);
            } catch (Exception e) {
                LoggerUtil.warn(String.format("unregister url false:%s", url), e);
            }
        }
    }

}
