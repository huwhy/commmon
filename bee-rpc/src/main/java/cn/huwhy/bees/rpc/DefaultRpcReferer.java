package cn.huwhy.bees.rpc;

import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.core.extension.ExtensionLoader;
import cn.huwhy.bees.exception.BeesServiceException;
import cn.huwhy.bees.transport.Client;
import cn.huwhy.bees.transport.EndpointFactory;
import cn.huwhy.bees.transport.TransportException;
import cn.huwhy.bees.util.LoggerUtil;

public class DefaultRpcReferer<T> extends AbstractReferer<T> {
        private Client          client;
        private EndpointFactory endpointFactory;

        public DefaultRpcReferer(Class<T> clz, URL url, URL serviceUrl) {
            super(clz, url, serviceUrl);

            endpointFactory =
                    ExtensionLoader.getExtensionLoader(EndpointFactory.class).getExtension(
                            url.getParameter(URLParamType.endpointFactory.getName(), URLParamType.endpointFactory.getValue()));

            client = endpointFactory.createClient(url);
        }

        @Override
        protected Response doCall(Request request) {
            try {
                // 为了能够实现跨group请求，需要使用server端的group。
                request.setAttachment(URLParamType.group.getName(), serviceUrl.getGroup());
                return client.request(request);
            } catch (TransportException exception) {
                throw new BeesServiceException("DefaultRpcReferer call Error: url=" + url.getUri(), exception);
            }
        }

        @Override
        protected void decrActiveCount(Request request, Response response) {
            if (response == null || !(response instanceof Future)) {
                activeRefererCount.decrementAndGet();
                return;
            }

            Future future = (Future) response;

            future.addListener(new FutureListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    activeRefererCount.decrementAndGet();
                }
            });
        }

        @Override
        protected boolean doInit() {
            boolean result = client.open();

            return result;
        }

        @Override
        public boolean isAvailable() {
            return client.isAvailable();
        }

        @Override
        public void destroy() {
            endpointFactory.safeReleaseResource(client, url);
            LoggerUtil.info("DefaultRpcReferer destory client: url={}" + url);
        }
    }