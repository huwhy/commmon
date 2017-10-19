package cn.huwhy.bees.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.huwhy.bees.cluster.Cluster;
import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.core.extension.ExtensionLoader;
import cn.huwhy.bees.exception.BeesErrorMsgConstant;
import cn.huwhy.bees.exception.BeesServiceException;
import cn.huwhy.bees.rpc.DefaultRequest;
import cn.huwhy.bees.rpc.Response;
import cn.huwhy.bees.switcher.Switcher;
import cn.huwhy.bees.switcher.SwitcherService;
import cn.huwhy.bees.util.ExceptionUtil;
import cn.huwhy.bees.util.LoggerUtil;
import cn.huwhy.bees.util.BeesFrameworkUtil;
import cn.huwhy.bees.util.ReflectUtil;
import cn.huwhy.bees.util.RequestIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefererInvocationHandler<T> implements InvocationHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private List<Cluster<T>> clusters;
    private Class<T>         clz;
    private SwitcherService switcherService = null;

    public RefererInvocationHandler(Class<T> clz, Cluster<T> cluster) {
        this.clz = clz;
        this.clusters = new ArrayList<>(1);
        this.clusters.add(cluster);

        init();
    }

    public RefererInvocationHandler(Class<T> clz, List<Cluster<T>> clusters) {
        this.clz = clz;
        this.clusters = clusters;

        init();
    }

    private void init() {
        // clusters 不应该为空
        String switchName =
                this.clusters.get(0).getUrl().getParameter(URLParamType.switcherService.getName(), URLParamType.switcherService.getValue());
        switcherService = ExtensionLoader.getExtensionLoader(SwitcherService.class).getExtension(switchName);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("toString".equals(method.getName())) {
            return method.toString();
        }
        DefaultRequest request = new DefaultRequest();

        request.setRequestId(RequestIdGenerator.getRequestId());
        request.setArguments(args);
        request.setMethodName(method.getName());
        request.setParamtersDesc(ReflectUtil.getMethodParamDesc(method));
        request.setInterfaceName(clz.getName());
        request.setAttachment(URLParamType.requestIdFromClient.getName(), String.valueOf(RequestIdGenerator.getRequestIdFromClient()));

        // 当 referer配置多个protocol的时候，比如A,B,C，
        // 那么正常情况下只会使用A，如果A被开关降级，那么就会使用B，B也被降级，那么会使用C
        for (Cluster<T> cluster : clusters) {
            Switcher switcher = switcherService.getSwitcher();

            if (switcher != null && !switcher.isOn()) {
                continue;
            }

            request.setAttachment(URLParamType.version.getName(), cluster.getUrl().getVersion());
            request.setAttachment(URLParamType.clientGroup.getName(), cluster.getUrl().getGroup());
            Response response;
            boolean throwException =
                    Boolean.parseBoolean(cluster.getUrl().getParameter(URLParamType.throwException.getName(),
                            URLParamType.throwException.getValue()));
            try {
                response = cluster.call(request);
                return response.getValue();
            } catch (RuntimeException e) {
                logger.error("", e);
                if (ExceptionUtil.isBizException(e)) {
                    Throwable t = e.getCause();
                    // 只抛出Exception，防止抛出远程的Error
                    if (t != null && t instanceof Exception) {
                        throw t;
                    } else {
                        String msg =
                                t == null ? "biz exception cause is null" : ("biz exception cause is throwable error:" + t.getClass()
                                        + ", errmsg:" + t.getMessage());
                        throw new BeesServiceException(msg, BeesErrorMsgConstant.SERVICE_DEFAULT_ERROR);
                    }
                } else if (!throwException) {
                    LoggerUtil.warn("RefererInvocationHandler invoke false, so return default value: uri=" + cluster.getUrl().getUri()
                            + " " + BeesFrameworkUtil.toString(request), e);
                    return getDefaultReturnValue(method.getReturnType());
                } else {
                    LoggerUtil.error(
                            "RefererInvocationHandler invoke Error: uri=" + cluster.getUrl().getUri() + " "
                                    + BeesFrameworkUtil.toString(request), e);
                    throw e;
                }
            }
        }

        throw new BeesServiceException("Referer call Error: cluster not exist, interface=" + clz.getName() + " "
                + BeesFrameworkUtil.toString(request), BeesErrorMsgConstant.SERVICE_UNFOUND);

    }

    private Object getDefaultReturnValue(Class<?> returnType) {
        if (returnType != null && returnType.isPrimitive()) {
            return PrimitiveDefault.getDefaultReturnValue(returnType);
        }
        return null;
    }

    private static class PrimitiveDefault {
        private static boolean defaultBoolean;
        private static char    defaultChar;
        private static byte    defaultByte;
        private static short   defaultShort;
        private static int     defaultInt;
        private static long    defaultLong;
        private static float   defaultFloat;
        private static double  defaultDouble;

        private static Map<Class<?>, Object> primitiveValues = new HashMap<Class<?>, Object>();

        static {
            primitiveValues.put(boolean.class, defaultBoolean);
            primitiveValues.put(char.class, defaultChar);
            primitiveValues.put(byte.class, defaultByte);
            primitiveValues.put(short.class, defaultShort);
            primitiveValues.put(int.class, defaultInt);
            primitiveValues.put(long.class, defaultLong);
            primitiveValues.put(float.class, defaultFloat);
            primitiveValues.put(double.class, defaultDouble);
        }

        public static Object getDefaultReturnValue(Class<?> returnType) {
            return primitiveValues.get(returnType);
        }

    }
}
