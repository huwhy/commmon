package cn.huwhy.bees.rpc;

import java.util.HashMap;
import java.util.Map;

import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.core.extension.ExtensionLoader;
import cn.huwhy.bees.transport.EndpointFactory;
import cn.huwhy.bees.transport.ProviderMessageRouter;
import cn.huwhy.bees.transport.ProviderProtectedMessageRouter;
import cn.huwhy.bees.transport.Server;
import cn.huwhy.bees.util.LoggerUtil;

public class DefaultRpcExporter<T> extends AbstractExporter<T> {
    // 多个service可能在相同端口进行服务暴露，因此来自同个端口的请求需要进行路由以找到相应的服务，同时不在该端口暴露的服务不应该被找到
    private static final Map<Integer, ProviderMessageRouter> ipPort2RequestRouter = new HashMap<>();

    private Server          server;
    private EndpointFactory endpointFactory;

    public DefaultRpcExporter(Provider<T> provider, URL url) {
        super(provider, url);

        ProviderMessageRouter requestRouter = initRequestRouter(url);
        endpointFactory =
                ExtensionLoader.getExtensionLoader(EndpointFactory.class).getExtension(
                        url.getParameter(URLParamType.endpointFactory.getName(), URLParamType.endpointFactory.getValue()));
        server = endpointFactory.createServer(url, requestRouter);
    }

    @Override
    public void unExport() {
        synchronized (ipPort2RequestRouter) {
            ProviderMessageRouter requestRouter = ipPort2RequestRouter.get(url.getPort());

            if (requestRouter != null) {
                requestRouter.removeProvider(provider);
            }
        }

        LoggerUtil.info("DefaultRpcExporter unExport Success: url={}", url);
    }

    @Override
    protected boolean doInit() {
        return server.open();
    }

    @Override
    public boolean isAvailable() {
        return server.isAvailable();
    }

    @Override
    public void destroy() {
        endpointFactory.safeReleaseResource(server, url);
        LoggerUtil.info("DefaultRpcExporter destroy Success: url={}", url);
    }

    private ProviderMessageRouter initRequestRouter(URL url) {
        ProviderMessageRouter requestRouter;
        synchronized (ipPort2RequestRouter) {
            requestRouter = ipPort2RequestRouter.get(url.getPort());
            if (requestRouter == null) {
                requestRouter = new ProviderProtectedMessageRouter();
                ipPort2RequestRouter.put(url.getPort(), requestRouter);
            }
            requestRouter.addProvider(provider);
        }

        return requestRouter;
    }
}