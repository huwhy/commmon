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

import java.util.ArrayList;
import java.util.List;

import cn.huwhy.bees.cluster.LoadBalance;
import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.core.extension.SpiMeta;
import cn.huwhy.bees.exception.BeesDoingException;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.exception.BeesServiceException;
import cn.huwhy.bees.exception.BeesTimeOutException;
import cn.huwhy.bees.rpc.Referer;
import cn.huwhy.bees.rpc.Request;
import cn.huwhy.bees.rpc.Response;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.util.ExceptionUtil;
import cn.huwhy.bees.util.LoggerUtil;

/**
 * Failover ha strategy.
 *
 * @author fishermen
 * @version V1.0 created at: 2013-5-21
 */
@SpiMeta(name = "failover")
public class FailoverHaStrategy<T> extends AbstractHaStrategy<T> {

    protected ThreadLocal<List<Referer<T>>> referersHolder = new ThreadLocal<List<Referer<T>>>() {
        @Override
        protected List<Referer<T>> initialValue() {
            return new ArrayList<Referer<T>>();
        }
    };

    @Override
    public Response call(Request request, LoadBalance<T> loadBalance) {

        List<Referer<T>> referers = selectReferers(request, loadBalance);
        if (referers.isEmpty()) {
            throw new BeesServiceException(String.format("FailoverHaStrategy No referers for request:%s, loadbalance:%s", request,
                    loadBalance));
        }
        URL refUrl = referers.get(0).getUrl();
        // 先使用method的配置
        int tryCount =
                refUrl.getMethodParameter(request.getMethodName(), request.getParamtersDesc(), URLParamType.retries.getName(),
                        URLParamType.retries.getIntValue());
        // 如果有问题，则设置为不重试
        if (tryCount < 0) {
            tryCount = 0;
        }
        int timeoutCnt = 0;
        for (int i = 0; i <= tryCount; ) {
            Referer<T> refer = referers.get(i % referers.size());
            try {
                request.setRetries(i);
                return refer.call(request);
            } catch (RuntimeException e) {
                // 对于业务异常，直接抛出
                if (ExceptionUtil.isBizException(e)) {
                    throw e;
                } else if (e instanceof BeesDoingException) {
                    i--;//后面i++ 这样后i不变
                    timeoutCnt++;
                    if (timeoutCnt > 3) {
                        throw new BeesTimeOutException();
                    }
                } else if (e instanceof BeesTimeOutException && timeoutCnt < 3) {
                    //超时请求 在当前refer 上请求3次
                    int timeout = refUrl.getMethodParameter(request.getMethodName(), request.getParamtersDesc(),
                            URLParamType.requestTimeout.getName(), URLParamType.requestTimeout.getIntValue());
                    LoggerUtil.debug("request timeout: {}", timeout);
                    timeoutCnt++;
                    continue;
                } else if (i >= tryCount) {
                    throw e;
                }
                LoggerUtil.warn(String.format("FailoverHaStrategy Call false for request:%s error=%s", request, e.getMessage()));
            }
            i++;
        }

        throw new BeesFrameworkException("FailoverHaStrategy.call should not come here!");
    }

    protected List<Referer<T>> selectReferers(Request request, LoadBalance<T> loadBalance) {
        List<Referer<T>> referers = referersHolder.get();
        referers.clear();
        loadBalance.selectToHolder(request, referers);
        return referers;
    }

}
