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

package cn.huwhy.bees.config.handler;

import java.util.Collection;
import java.util.List;

import cn.huwhy.bees.cluster.Cluster;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.cluster.support.ClusterSupport;
import cn.huwhy.bees.core.extension.Scope;
import cn.huwhy.bees.core.extension.Spi;
import cn.huwhy.bees.rpc.Exporter;

/**
 * 
 * Handle urls which are from config.
 *
 * @author fishermen
 * @version V1.0 created at: 2013-5-31
 */
@Spi(scope = Scope.SINGLETON)
public interface ConfigHandler {

    <T> ClusterSupport<T> buildClusterSupport(Class<T> interfaceClass, List<URL> registryUrls);

    <T> T refer(Class<T> interfaceClass, List<Cluster<T>> cluster, String proxyType);

    <T> Exporter<T> export(Class<T> interfaceClass, T ref, List<URL> registryUrls);

    <T> void unExport(List<Exporter<T>> exporters, Collection<URL> registryUrls);
}
