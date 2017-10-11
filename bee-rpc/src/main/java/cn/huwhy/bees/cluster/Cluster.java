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

package cn.huwhy.bees.cluster;

import java.util.List;

import cn.huwhy.bees.rpc.Caller;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.core.extension.Scope;
import cn.huwhy.bees.core.extension.Spi;
import cn.huwhy.bees.rpc.Referer;

/**
 * 
 * Cluster is a service broker, used to
 * 
 * @author fishermen
 * @version V1.0 created at: 2013-5-16
 */
@Spi(scope = Scope.PROTOTYPE)
public interface Cluster<T> extends Caller<T> {

    @Override
    void init();

    void setUrl(URL url);

    void setLoadBalance(LoadBalance<T> loadBalance);

    void setHaStrategy(HaStrategy<T> haStrategy);

    void onRefresh(List<Referer<T>> referers);

    List<Referer<T>> getReferers();

    LoadBalance<T> getLoadBalance();
}
