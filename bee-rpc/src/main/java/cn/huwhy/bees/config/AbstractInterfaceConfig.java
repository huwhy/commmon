package cn.huwhy.bees.config;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import cn.huwhy.bees.common.BeesConstants;
import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.exception.BeesErrorMsgConstant;
import cn.huwhy.bees.exception.BeesServiceException;
import cn.huwhy.bees.registry.RegistryService;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.util.NetUtils;
import cn.huwhy.bees.util.UrlUtils;

/**
 * <pre>
 * Interface config，
 *
 * 配置约定
 * 	  1 service 和 referer 端相同的参数的含义一定相同；
 *    2 service端参数的覆盖策略：protocol--basicConfig--service，前面的配置会被后面的config参数覆盖；
 *    3 registry 参数不进入service、referer端的参数列表；
 *    4 referer端从注册中心拿到参数后，先用referer端的参数覆盖，然后再使用该service
 * </pre>
 */

public class AbstractInterfaceConfig extends AbstractConfig {

    private static final long serialVersionUID = 4776516803466933310L;

    // 注册中心的配置列表
    protected List<RegistryConfig> registries;

    // 应用名称
    protected String application;

    // 模块名称
    protected String module;

    // 分组
    protected String group;

    // 服务版本
    protected String version;

    // 代理类型
    protected String proxy;

    // 过滤器
    protected String filter;

    // 最大并发调用
    protected Integer actives;

    // 是否异步
    protected Boolean async;

    // 是否共享 channel
    protected Boolean shareChannel;

    // if throw exception when call failure，the default value is ture
    protected Boolean throwException;

    // 请求超时时间
    protected Integer requestTimeout;

    // 是否注册
    protected Boolean register;

    // 是否记录访问日志，true记录，false不记录
    protected String accessLog;

    // 是否进行check，如果为true，则在监测失败后抛异常
    protected String check;

    // 重试次数
    protected Integer retries;

    // 是否开启gzip压缩
    protected Boolean usegz;

    // 进行gzip压缩的最小阈值，usegz开启，且大于此值时才进行gzip压缩。单位Byte
    protected Integer mingzSize;

    protected String codec;

    protected String localServiceAddress;

    protected String idempotent;

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getAccessLog() {
        return accessLog;
    }

    public void setAccessLog(String accessLog) {
        this.accessLog = accessLog;
    }

    public List<RegistryConfig> getRegistries() {
        return registries;
    }

    public void setRegistries(List<RegistryConfig> registries) {
        this.registries = registries;
    }

    public void setRegistry(RegistryConfig registry) {
        this.registries = Collections.singletonList(registry);
    }

    public Integer getActives() {
        return actives;
    }

    public void setActives(Integer actives) {
        this.actives = actives;
    }

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    @Deprecated
    public void setCheck(Boolean check) {
        this.check = String.valueOf(check);
    }

    public Boolean getShareChannel() {
        return shareChannel;
    }

    public void setShareChannel(Boolean shareChannel) {
        this.shareChannel = shareChannel;
    }

    public Boolean getThrowException() {
        return throwException;
    }

    public void setThrowException(Boolean throwException) {
        this.throwException = throwException;
    }

    public Integer getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public Boolean getRegister() {
        return register;
    }

    public void setRegister(Boolean register) {
        this.register = register;
    }

    public String getLocalServiceAddress() {
        return localServiceAddress;
    }

    public void setLocalServiceAddress(String localServiceAddress) {
        this.localServiceAddress = localServiceAddress;
    }

    public Boolean getUsegz() {
        return usegz;
    }

    public void setUsegz(Boolean usegz) {
        this.usegz = usegz;
    }

    public Integer getMingzSize() {
        return mingzSize;
    }

    public void setMingzSize(Integer mingzSize) {
        this.mingzSize = mingzSize;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public String getIdempotent() {
        return idempotent;
    }

    public void setIdempotent(String idempotent) {
        this.idempotent = idempotent;
    }

    protected List<URL> loadRegistryUrls() {
        List<URL> registryList = new ArrayList<URL>();
        if (registries != null && !registries.isEmpty()) {
            for (RegistryConfig config : registries) {
                String address = config.getAddress();
                if (StringUtils.isBlank(address)) {
                    address = NetUtils.LOCALHOST + ":" + BeesConstants.DEFAULT_INT_VALUE;
                }
                Map<String, String> map = new HashMap<String, String>();
                config.appendConfigParams(map);

                map.put(URLParamType.application.getName(), getApplication());
                map.put(URLParamType.path.getName(), RegistryService.class.getName());
                map.put(URLParamType.refreshTimestamp.getName(), String.valueOf(System.currentTimeMillis()));

                // 设置默认的registry protocol，parse完protocol后，需要去掉该参数
                if (!map.containsKey(URLParamType.protocol.getName())) {
                    if (address.contains("://")) {
                        map.put(URLParamType.protocol.getName(), address.substring(0, address.indexOf("://")));
                    }
                    map.put(URLParamType.protocol.getName(), BeesConstants.REGISTRY_PROTOCOL_LOCAL);
                }
                // address内部可能包含多个注册中心地址
                List<URL> urls = UrlUtils.parseURLs(address, map);
                if (urls != null && !urls.isEmpty()) {
                    for (URL url : urls) {
                        url.removeParameter(URLParamType.protocol.getName());
                        registryList.add(url);
                    }
                }
            }
        }
        return registryList;
    }

    protected void checkInterfaceAndMethods(Class<?> interfaceClass) {
        if (interfaceClass == null) {
            throw new IllegalStateException("interface not allow null!");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
    }

    protected String getLocalHostAddress(List<URL> registryURLs) {

        String localAddress = null;

        Map<String, Integer> regHostPorts = new HashMap<String, Integer>();
        for (URL ru : registryURLs) {
            if (StringUtils.isNotBlank(ru.getHost()) && ru.getPort() > 0) {
                regHostPorts.put(ru.getHost(), ru.getPort());
            }
        }

        InetAddress address = NetUtils.getLocalAddress(regHostPorts);
        if (address != null) {
            localAddress = address.getHostAddress();
        }

        if (NetUtils.isValidLocalHost(localAddress)) {
            return localAddress;
        }
        throw new BeesServiceException("Please config local server hostname with intranet IP first!",
                BeesErrorMsgConstant.FRAMEWORK_INIT_ERROR);
    }
}
