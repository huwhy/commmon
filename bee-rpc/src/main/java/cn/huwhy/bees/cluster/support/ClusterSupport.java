package cn.huwhy.bees.cluster.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import cn.huwhy.bees.cluster.Cluster;
import cn.huwhy.bees.cluster.HaStrategy;
import cn.huwhy.bees.cluster.LoadBalance;
import cn.huwhy.bees.common.BeesConstants;
import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.core.extension.ExtensionLoader;
import cn.huwhy.bees.exception.BeesErrorMsgConstant;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.registry.NotifyListener;
import cn.huwhy.bees.registry.Registry;
import cn.huwhy.bees.registry.RegistryFactory;
import cn.huwhy.bees.rpc.DefaultRpcReferer;
import cn.huwhy.bees.rpc.Protocol;
import cn.huwhy.bees.rpc.Referer;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.util.CollectionUtil;
import cn.huwhy.bees.util.LoggerUtil;
import cn.huwhy.bees.util.StringTools;

public class ClusterSupport<T> implements NotifyListener {

    private static ConcurrentHashMap<String, Protocol> protocols = new ConcurrentHashMap<String, Protocol>();
    private Cluster<T> cluster;
    private List<URL>  registryUrls;
    private URL        url;
    private Class<T>   interfaceClass;
    private ConcurrentHashMap<URL, List<Referer<T>>> registryReferers = new ConcurrentHashMap<URL, List<Referer<T>>>();

    public ClusterSupport(Class<T> interfaceClass, List<URL> registryUrls) {
        this.registryUrls = registryUrls;
        this.interfaceClass = interfaceClass;
        String urlStr = StringTools.urlDecode(registryUrls.get(0).getParameter(URLParamType.embed.getName()));
        this.url = URL.valueOf(urlStr);
    }

    public void init() {

        prepareCluster();

        URL subUrl = toSubscribeUrl(url);
        for (URL ru : registryUrls) {

            String directUrlStr = ru.getParameter(URLParamType.directUrl.getName());
            // 如果有directUrl，直接使用这些directUrls进行初始化，不用到注册中心discover
            if (StringUtils.isNotBlank(directUrlStr)) {
                List<URL> directUrls = parseDirectUrls(directUrlStr);
                if (!directUrls.isEmpty()) {
                    notify(ru, directUrls);
                    LoggerUtil.info("Use direct urls, refUrl={}, directUrls={}", url, directUrls);
                    continue;
                }
            }

            // client 注册自己，同时订阅service列表
            Registry registry = getRegistry(ru);
            registry.subscribe(subUrl, this);
        }

        boolean check = Boolean.parseBoolean(url.getParameter(URLParamType.check.getName(), URLParamType.check.getValue()));
        if (!CollectionUtil.isEmpty(cluster.getReferers()) || !check) {
            cluster.init();
            if (CollectionUtil.isEmpty(cluster.getReferers()) && !check) {
                LoggerUtil.warn(String.format("refer:%s", this.url.getPath() + "/" + this.url.getVersion()), "No services");
            }
            return;
        }

        throw new BeesFrameworkException(String.format("ClusterSupport No service urls for the refer:%s, registries:%s",
                this.url.getIdentity(), registryUrls), BeesErrorMsgConstant.SERVICE_UNFOUND);
    }

    public void destroy() {
        URL subscribeUrl = toSubscribeUrl(url);
        for (URL ru : registryUrls) {
            try {
                Registry registry = getRegistry(ru);
                registry.unsubscribe(subscribeUrl, this);
                if (!BeesConstants.NODE_TYPE_REFERER.equals(url.getParameter(URLParamType.nodeType.getName()))) {
                    registry.unregister(url);
                }
            } catch (Exception e) {
                LoggerUtil.warn(String.format("Unregister or unsubscribe false for url (%s), registry= %s", url, ru.getIdentity()), e);
            }

        }
        try {
            getCluster().destroy();
        } catch (Exception e) {
            LoggerUtil.warn(String.format("Exception when destroy cluster: %s", getCluster().getUrl()));
        }
    }

    protected Registry getRegistry(URL url) {
        RegistryFactory registryFactory = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getExtension("zookeeper");
        return registryFactory.getRegistry(url);
    }

    private URL toSubscribeUrl(URL url) {
        URL subUrl = url.createCopy();
        subUrl.addParameter(URLParamType.nodeType.getName(), BeesConstants.NODE_TYPE_SERVICE);
        return subUrl;
    }

    /**
     * <pre>
     * 1 notify的执行需要串行
     * 2 notify通知都是全量通知，在设入新的referer后，cluster需要把不再使用的referer进行回收，避免资源泄漏;
     * 3 如果该registry对应的referer数量为0，而没有其他可用的referers，那就忽略该次通知；
     * 4 此处对protoco进行decorator处理，当前为增加filters
     * </pre>
     */
    @Override
    public synchronized void notify(URL registryUrl, List<URL> urls) {
        if (CollectionUtil.isEmpty(urls)) {
            onRegistryEmpty(registryUrl);
            LoggerUtil.warn("ClusterSupport config change notify, urls is empty: registry={} service={} urls=[]", registryUrl.getUri(),
                    url.getIdentity());

            return;
        }

        LoggerUtil.info("ClusterSupport config change notify: registry={} service={} urls={}", registryUrl.getUri(), url.getIdentity(),
                getIdentities(urls));

        // 通知都是全量通知，在设入新的referer后，cluster内部需要把不再使用的referer进行回收，避免资源泄漏
        // ////////////////////////////////////////////////////////////////////////////////

        // 判断urls中是否包含权重信息，并通知loadbalance。
        processWeights(urls);

        List<Referer<T>> newReferers = new ArrayList<Referer<T>>();
        for (URL u : urls) {
            if (!u.canServe(url)) {
                continue;
            }
            Referer<T> referer = getExistingReferer(u, registryReferers.get(registryUrl));
            if (referer == null) {
                // careful u: serverURL, refererURL的配置会被serverURL的配置覆盖
                URL refererURL = u.createCopy();
                mergeClientConfigs(refererURL);
                referer = new DefaultRpcReferer<>(interfaceClass, refererURL, u);
                referer.init();
            }
            if (referer != null) {
                newReferers.add(referer);
            }
        }

        if (CollectionUtil.isEmpty(newReferers)) {
            onRegistryEmpty(registryUrl);
            return;
        }

        // 此处不销毁referers，由cluster进行销毁
        registryReferers.put(registryUrl, newReferers);
        refreshCluster();
    }

    /**
     * 检查urls中的第一个url是否为权重信息。 如果是权重信息则把权重信息传递给loadbalance，并移除权重url。
     *
     * @param urls
     */
    private void processWeights(List<URL> urls) {
        if (urls != null && !urls.isEmpty()) {
            URL ruleUrl = urls.get(0);
            // 没有权重时需要传递默认值。因为可能是变更时去掉了权重
            String weights = URLParamType.weights.getValue();
            if ("rule".equalsIgnoreCase(ruleUrl.getHost())) {
                weights = ruleUrl.getParameter(URLParamType.weights.getName(), URLParamType.weights.getValue());
                urls.remove(0);
            }
            LoggerUtil.info("refresh weight. weight=" + weights);
            this.cluster.getLoadBalance().setWeightString(weights);
        }
    }

    private void onRegistryEmpty(URL excludeRegistryUrl) {
        boolean noMoreOtherRefers = registryReferers.size() == 1 && registryReferers.containsKey(excludeRegistryUrl);
        if (noMoreOtherRefers) {
            LoggerUtil.warn(String.format("Ignore notify for no more referers in this cluster, registry: %s, cluster=%s",
                    excludeRegistryUrl, getUrl()));
        } else {
            registryReferers.remove(excludeRegistryUrl);
            refreshCluster();
        }
    }

    private Referer<T> getExistingReferer(URL url, List<Referer<T>> referers) {
        if (referers == null) {
            return null;
        }
        for (Referer<T> r : referers) {
            if (ObjectUtils.equals(url, r.getUrl()) || ObjectUtils.equals(url, r.getServiceUrl())) {
                return r;
            }
        }
        return null;
    }

    /**
     * refererURL的扩展参数中，除了application、module外，其他参数被client覆盖， 如果client没有则使用referer的参数
     *
     * @param refererURL
     */
    private void mergeClientConfigs(URL refererURL) {
        String application = refererURL.getParameter(URLParamType.application.getName(), URLParamType.application.getValue());
        String module = refererURL.getParameter(URLParamType.module.getName(), URLParamType.module.getValue());
        refererURL.addParameters(this.url.getParameters());

        refererURL.addParameter(URLParamType.application.getName(), application);
        refererURL.addParameter(URLParamType.module.getName(), module);
    }

    private void refreshCluster() {
        List<Referer<T>> referers = new ArrayList<Referer<T>>();
        for (List<Referer<T>> refs : registryReferers.values()) {
            referers.addAll(refs);
        }
        cluster.onRefresh(referers);
    }

    public Cluster<T> getCluster() {
        return cluster;
    }

    public URL getUrl() {
        return url;
    }

    private String getIdentities(List<URL> urls) {
        if (urls == null || urls.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (URL u : urls) {
            builder.append(u.getIdentity()).append(",");
        }
        builder.setLength(builder.length() - 1);
        builder.append("]");

        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private void prepareCluster() {
        String clusterName = url.getParameter(URLParamType.cluster.getName(), URLParamType.cluster.getValue());
        String loadbalanceName = url.getParameter(URLParamType.loadbalance.getName(), URLParamType.loadbalance.getValue());
        String haStrategyName = url.getParameter(URLParamType.haStrategy.getName(), URLParamType.haStrategy.getValue());

        cluster = ExtensionLoader.getExtensionLoader(Cluster.class).getExtension(clusterName);
        LoadBalance<T> loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(loadbalanceName);
        HaStrategy<T> ha = ExtensionLoader.getExtensionLoader(HaStrategy.class).getExtension(haStrategyName);
        cluster.setLoadBalance(loadBalance);
        cluster.setHaStrategy(ha);
        cluster.setUrl(url);
    }

    private List<URL> parseDirectUrls(String directUrlStr) {
        String[] durlArr = BeesConstants.COMMA_SPLIT_PATTERN.split(directUrlStr);
        List<URL> directUrls = new ArrayList<URL>();
        for (String dus : durlArr) {
            URL du = URL.valueOf(StringTools.urlDecode(dus));
            if (du != null) {
                directUrls.add(du);
            }
        }
        return directUrls;
    }
}
