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

package cn.huwhy.bees.transport;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cn.huwhy.bees.exception.BeesBizException;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.exception.BeesServiceException;
import cn.huwhy.bees.protocol.rpc.CompressRpcCodec;
import cn.huwhy.bees.rpc.DefaultResponse;
import cn.huwhy.bees.rpc.Provider;
import cn.huwhy.bees.rpc.Request;
import cn.huwhy.bees.rpc.Response;
import cn.huwhy.bees.util.LoggerUtil;
import cn.huwhy.bees.util.BeesFrameworkUtil;
import cn.huwhy.bees.util.ReflectUtil;

/**
 * service 消息处理
 * 
 * <pre>
 * 		1） 多个service的支持
 * 		2） 区分service的方式： group/interface/version
 * </pre>
 * 
 * @author maijunsheng
 * @version 创建时间：2013-6-4
 * 
 */
public class ProviderMessageRouter implements MessageHandler {
    private Map<String, Provider<?>> providers = new HashMap<>();

    // 所有暴露出去的方法计数
    // 比如：messageRouter 里面涉及2个Service: ServiceA 有5个public method，ServiceB
    // 有10个public method，那么就是15
    AtomicInteger methodCounter = new AtomicInteger(0);

    ProviderMessageRouter() {}

    @Override
    public Object handle(Channel channel, Object message) {
        if (channel == null || message == null) {
            throw new BeesFrameworkException("RequestRouter handler(channel, message) params is null");
        }

        if (!(message instanceof Request)) {
            throw new BeesFrameworkException("RequestRouter message type not support: " + message.getClass());
        }

        Request request = (Request) message;

        String serviceKey = BeesFrameworkUtil.getServiceKey(request);

        Provider<?> provider = providers.get(serviceKey);

        if (provider == null) {
            LoggerUtil.error(this.getClass().getSimpleName() + " handler Error: provider not exist serviceKey=" + serviceKey + " "
                    + BeesFrameworkUtil.toString(request));
            BeesServiceException exception =
                    new BeesServiceException(this.getClass().getSimpleName() + " handler Error: provider not exist serviceKey="
                            + serviceKey + " " + BeesFrameworkUtil.toString(request));

            DefaultResponse response = new DefaultResponse();
            response.setException(exception);
            return response;
        }

        return call(request, provider);
    }

    protected Response call(Request request, Provider<?> provider) {
        try {
            return provider.call(request);
        } catch (Exception e) {
            DefaultResponse response = new DefaultResponse();
            response.setException(new BeesBizException(e.getMessage()));
            return response;
        }
    }

    public synchronized void addProvider(Provider<?> provider) {
        String serviceKey = BeesFrameworkUtil.getServiceKey(provider.getUrl());
        if (providers.containsKey(serviceKey)) {
            throw new BeesFrameworkException("provider already exist: " + serviceKey);
        }

        providers.put(serviceKey, provider);

        // 获取该service暴露的方法数：
        List<Method> methods = ReflectUtil.getPublicMethod(provider.getInterface());
        CompressRpcCodec.putMethodSign(provider, methods);// 对所有接口方法生成方法签名。适配方法签名压缩调用方式。

        int publicMethodCount = methods.size();
        methodCounter.addAndGet(publicMethodCount);

        LoggerUtil.info("RequestRouter addProvider: url=" + provider.getUrl() + " all_public_method_count=" + methodCounter.get());
    }

    public synchronized void removeProvider(Provider<?> provider) {
        String serviceKey = BeesFrameworkUtil.getServiceKey(provider.getUrl());

        providers.remove(serviceKey);
        List<Method> methods = ReflectUtil.getPublicMethod(provider.getInterface());
        int publicMethodCount = methods.size();
        methodCounter.getAndSet(methodCounter.get() - publicMethodCount);

        LoggerUtil.info("RequestRouter removeProvider: url=" + provider.getUrl() + " all_public_method_count=" + methodCounter.get());
    }

}
