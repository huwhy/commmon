package cn.huwhy.bees.util;

import org.apache.commons.lang3.StringUtils;

import cn.huwhy.bees.common.BeesConstants;
import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.config.RegistryConfig;
import cn.huwhy.bees.rpc.Request;
import cn.huwhy.bees.rpc.URL;

/**
 * 提供框架内部一些约定处理
 *
 */
public class BeesFrameworkUtil {

    /**
     * 目前根据 group/interface/version 来唯一标示一个服务
     *
     * @param request
     * @return
     */

    public static String getServiceKey(Request request) {
        String version = getVersionFromRequest(request);
        String group = getGroupFromRequest(request);

        return getServiceKey(group, request.getInterfaceName(), version);
    }

    public static String getGroupFromRequest(Request request) {
        return getValueFromRequest(request, URLParamType.group.name(), URLParamType.group.getValue());
    }

    public static String getVersionFromRequest(Request request) {
        return getValueFromRequest(request, URLParamType.version.name(), URLParamType.version.getValue());
    }

    public static String getValueFromRequest(Request request, String key, String defaultValue) {
        String value = defaultValue;
        if (request.getAttachments() != null && request.getAttachments().containsKey(key)) {
            value = request.getAttachments().get(key);
        }
        return value;
    }

    /**
     * 目前根据 group/interface/version 来唯一标示一个服务
     *
     * @param url
     * @return
     */
    public static String getServiceKey(URL url) {
        return getServiceKey(url.getGroup(), url.getPath(), url.getVersion());
    }

    /**
     * protocol key: protocol://host:port/group/interface/version
     *
     * @param url
     * @return
     */
    public static String getProtocolKey(URL url) {
        return url.getServerPortStr() + BeesConstants.PATH_SEPARATOR
                + url.getGroup() + BeesConstants.PATH_SEPARATOR + url.getPath() + BeesConstants.PATH_SEPARATOR + url.getVersion();
    }

    /**
     * 输出请求的关键信息： requestId=** interface=** method=**(**)
     *
     * @param request
     * @return
     */
    public static String toString(Request request) {
        return "requestId=" + request.getRequestId() + " interface=" + request.getInterfaceName() + " method=" + request.getMethodName()
                + "(" + request.getParamtersDesc() + ")";
    }

    /**
     * 根据Request得到 interface.method(paramDesc) 的 desc
     * <p>
     * <pre>
     * 		比如：
     * 			package com.weibo.api.motan;
     *
     * 		 	interface A { public hello(int age); }
     *
     * 			那么return "com.weibo.api.motan.A.hell(int)"
     * </pre>
     *
     * @param request
     * @return
     */
    public static String getFullMethodString(Request request) {
        return request.getInterfaceName() + "." + request.getMethodName() + "("
                + request.getParamtersDesc() + ")";
    }

    public static String getGroupMethodString(Request request) {
        return getGroupFromRequest(request) + "_" + getFullMethodString(request);
    }

    /**
     * 判断url:source和url:target是否可以使用共享的service channel(port) 对外提供服务
     * <p>
     * <pre>
     * 		1） protocol
     * 		2） codec
     * 		3） serialize
     * 		4） maxContentLength
     * 		5） maxServerConnection
     * 		6） maxWorkerThread
     * 		7） workerQueueSize
     * 		8） heartbeatFactory
     * </pre>
     *
     * @param source
     * @param target
     * @return
     */
    public static boolean checkIfCanShallServiceChannel(URL source, URL target) {
        if (!StringUtils.equals(source.getParameter(URLParamType.codec.getName()), target.getParameter(URLParamType.codec.getName()))) {
            return false;
        }

        if (!StringUtils.equals(source.getParameter(URLParamType.serialize.getName()),
                target.getParameter(URLParamType.serialize.getName()))) {
            return false;
        }

        if (!StringUtils.equals(source.getParameter(URLParamType.maxContentLength.getName()),
                target.getParameter(URLParamType.maxContentLength.getName()))) {
            return false;
        }

        if (!StringUtils.equals(source.getParameter(URLParamType.maxServerConnection.getName()),
                target.getParameter(URLParamType.maxServerConnection.getName()))) {
            return false;
        }

        if (!StringUtils.equals(source.getParameter(URLParamType.maxWorkerThread.getName()),
                target.getParameter(URLParamType.maxWorkerThread.getName()))) {
            return false;
        }

        if (!StringUtils.equals(source.getParameter(URLParamType.workerQueueSize.getName()),
                target.getParameter(URLParamType.workerQueueSize.getName()))) {
            return false;
        }

        return StringUtils.equals(source.getParameter(URLParamType.heartbeatFactory.getName()),
                target.getParameter(URLParamType.heartbeatFactory.getName()));

    }

    /**
     * 判断url:source和url:target是否可以使用共享的client channel(port) 对外提供服务
     * <p>
     * <pre>
     * 		1） protocol
     * 		2） codec
     * 		3） serialize
     * 		4） maxContentLength
     * 		5） maxClientConnection
     * 		6） heartbeatFactory
     * </pre>
     *
     * @param source
     * @param target
     * @return
     */
    public static boolean checkIfCanShallClientChannel(URL source, URL target) {
        if (!StringUtils.equals(source.getParameter(URLParamType.codec.getName()), target.getParameter(URLParamType.codec.getName()))) {
            return false;
        }

        if (!StringUtils.equals(source.getParameter(URLParamType.serialize.getName()),
                target.getParameter(URLParamType.serialize.getName()))) {
            return false;
        }

        if (!StringUtils.equals(source.getParameter(URLParamType.maxContentLength.getName()),
                target.getParameter(URLParamType.maxContentLength.getName()))) {
            return false;
        }

        if (!StringUtils.equals(source.getParameter(URLParamType.maxClientConnection.getName()),
                target.getParameter(URLParamType.maxClientConnection.getName()))) {
            return false;
        }

        return StringUtils.equals(source.getParameter(URLParamType.heartbeatFactory.getName()),
                target.getParameter(URLParamType.heartbeatFactory.getName()));

    }

    /**
     * serviceKey: group/interface/version
     *
     * @param group
     * @param interfaceName
     * @param version
     * @return
     */
    private static String getServiceKey(String group, String interfaceName, String version) {
        return group + BeesConstants.PATH_SEPARATOR + interfaceName + BeesConstants.PATH_SEPARATOR + version;
    }

    /**
     * 默认本地注册中心
     *
     * @return local registry
     */
    public static RegistryConfig getDefaultRegistryConfig() {
        RegistryConfig local = new RegistryConfig();
        local.setRegProtocol("local");
        return local;
    }

}
