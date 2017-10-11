package cn.huwhy.bees.common;

import cn.huwhy.bees.config.RegistryConfig;

public enum URLParamType {
    /** version **/
    version("version", BeesConstants.DEFAULT_VERSION),
    /** request timeout **/
    requestTimeout("requestTimeout", 7000),
    /** request id from http interface **/
    requestIdFromClient("requestIdFromClient", 0),
    /** connect timeout **/
    connectTimeout("connectTimeout", 5000),
    /** service min worker threads **/
    minWorkerThread("minWorkerThread", 20),
    /** service max worker threads **/
    maxWorkerThread("maxWorkerThread", 200),
    /** pool min conn number **/
    minClientConnection("minClientConnection", 2),
    /** pool max conn number **/
    maxClientConnection("maxClientConnection", 10),
    /** pool max conn number **/
    maxContentLength("maxContentLength", 10 * 1024 * 1024),
    /** max server conn (all clients conn) **/
    maxServerConnection("maxServerConnection", 100000),
    /** pool conn manger stragy **/
    poolLifo("poolLifo", true),

    lazyInit("lazyInit", false),
    /** multi referer share the same channel **/
    shareChannel("shareChannel", false),

    /************************** SPI start ******************************/

    /** serialize **/
    serialize("serialization", "hessian2"),
    /** codec **/
    codec("codec", "bees"),
    /** endpointFactory **/
    endpointFactory("endpointFactory", "bees"),
    /** heartbeatFactory **/
    heartbeatFactory("heartbeatFactory", "bees"),
    /** switcherService **/
    switcherService("switcherService", "localSwitcherService"),

    /************************** SPI end ******************************/

    group("group", "default_rpc"), 
    clientGroup("clientGroup", "default_rpc"), 
    accessLog("accessLog", false),

    // 0为不做并发限制
    actives("actives", 0),

    refreshTimestamp("refreshTimestamp", 0), 
    nodeType("nodeType", BeesConstants.NODE_TYPE_SERVICE),

    // 格式为protocol:port
    export("export", ""),
    embed("embed", ""),

    registryRetryPeriod("registryRetryPeriod", 30 * BeesConstants.SECOND_MILLS),
    /* 注册中心不可用节点剔除方式 */
    excise("excise", RegistryConfig.Excise.excise_dynamic.getName()),
    cluster("cluster", BeesConstants.DEFAULT_VALUE),
    loadbalance("loadbalance", "activeWeight"), 
    haStrategy("haStrategy", "failover"), 
    protocol("protocol", BeesConstants.PROTOCOL_MOTAN),
    path("path", ""), 
    host("host", ""), 
    port("port", 0),
    idempotent("idempotent", "default"),
    iothreads("iothreads", Runtime.getRuntime().availableProcessors() + 1), 
    workerQueueSize("workerQueueSize", 0), 
    acceptConnections("acceptConnections", 0), 
    proxy("proxy", BeesConstants.PROXY_JDK),
    filter("filter", ""),

    usegz("useGz", false), // 是否开启gzip压缩
    mingzSize("minGzSize", 1000), // 进行gz压缩的最小数据大小。超过此阈值才进行gz压缩


    application("application", BeesConstants.FRAMEWORK_NAME),
    module("module", BeesConstants.FRAMEWORK_NAME),

    retries("retries", 0), 
    async("async", false), 
    errorRate("errorRate", "0.01"),
    check("check", "true"), 
    directUrl("directUrl", ""), 
    registrySessionTimeout("registrySessionTimeout", 5 * BeesConstants.MINUTE_MILLS),

    register("register", true), 
    subscribe("subscribe", true), 
    throwException("throwException", "true"),

    localServiceAddress("localServiceAddress", ""),

    // 切换group时，各个group的权重比。默认无权重
    weights("weights", "");

    private String name;
    private String value;
    private long longValue;
    private int intValue;
    private boolean boolValue;

    URLParamType(String name, String value) {
        this.name = name;
        this.value = value;
    }

    URLParamType(String name, long longValue) {
        this.name = name;
        this.value = String.valueOf(longValue);
        this.longValue = longValue;
    }

    URLParamType(String name, int intValue) {
        this.name = name;
        this.value = String.valueOf(intValue);
        this.intValue = intValue;
    }

    URLParamType(String name, boolean boolValue) {
        this.name = name;
        this.value = String.valueOf(boolValue);
        this.boolValue = boolValue;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getIntValue() {
        return intValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public boolean getBooleanValue() {
        return boolValue;
    }

}
