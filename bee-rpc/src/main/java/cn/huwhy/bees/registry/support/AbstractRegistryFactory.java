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

package cn.huwhy.bees.registry.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import cn.huwhy.bees.exception.BeesErrorMsgConstant;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.registry.Registry;
import cn.huwhy.bees.registry.RegistryFactory;
import cn.huwhy.bees.rpc.URL;

/**
 * 
 * Create and cache registry.
 * 
 * @author fishermen
 * @version V1.0 created at: 2013-5-28
 */

public abstract class AbstractRegistryFactory implements RegistryFactory {

    private static ConcurrentHashMap<String, Registry> registries = new ConcurrentHashMap<String, Registry>();

    private static final ReentrantLock lock = new ReentrantLock();

    protected String getRegistryUri(URL url) {
        String registryUri = url.getUri();
        return registryUri;
    }

    @Override
    public Registry getRegistry(URL url) {
        String registryUri = getRegistryUri(url);
        try {
            lock.lock();
            Registry registry = registries.get(registryUri);
            if (registry != null) {
                return registry;
            }
            registry = createRegistry(url);
            if (registry == null) {
                throw new BeesFrameworkException("Create registry false for url:" + url, BeesErrorMsgConstant.FRAMEWORK_INIT_ERROR);
            }
            registries.put(registryUri, registry);
            return registry;
        } catch (Exception e) {
            throw new BeesFrameworkException("Create registry false for url:" + url, e, BeesErrorMsgConstant.FRAMEWORK_INIT_ERROR);
        } finally {
            lock.unlock();
        }
    }

    protected abstract Registry createRegistry(URL url);
}
