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

package cn.huwhy.bees.cluster.ha;

import cn.huwhy.bees.rpc.Request;
import cn.huwhy.bees.cluster.LoadBalance;
import cn.huwhy.bees.core.extension.SpiMeta;
import cn.huwhy.bees.rpc.Referer;
import cn.huwhy.bees.rpc.Response;

/**
 * 
 * Fail fast ha strategy.
 *
 * @author fishermen
 * @version V1.0 created at: 2013-5-22
 */
@SpiMeta(name = "failfast")
public class FailfastHaStrategy<T> extends AbstractHaStrategy<T> {

    @Override
    public Response call(Request request, LoadBalance<T> loadBalance) {
        Referer<T> refer = loadBalance.select(request);
        return refer.call(request);
    }
}
