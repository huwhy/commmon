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

package cn.huwhy.bees.rpc;

import cn.huwhy.bees.exception.BeesErrorMsgConstant;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.util.LoggerUtil;

/**
 * @author maijunsheng
 * @version 创建时间：2013-5-21
 * 
 */
public abstract class AbstractNode implements Node {

    protected URL url;

    protected volatile boolean init = false;
    protected volatile boolean available = false;

    public AbstractNode(URL url) {
        this.url = url;
    }

    @Override
    public synchronized void init() {
        if (init) {
            LoggerUtil.warn(this.getClass().getSimpleName() + " node already init: " + desc());
            return;
        }

        boolean result = doInit();

        if (!result) {
            LoggerUtil.error(this.getClass().getSimpleName() + " node init Error: " + desc());
            throw new BeesFrameworkException(this.getClass().getSimpleName() + " node init Error: " + desc(),
                    BeesErrorMsgConstant.FRAMEWORK_INIT_ERROR);
        } else {
            LoggerUtil.info(this.getClass().getSimpleName() + " node init Success: " + desc());

            init = true;
            available = true;
        }
    }

    protected abstract boolean doInit();

    @Override
    public boolean isAvailable() {
        return available;
    }

    public URL getUrl() {
        return url;
    }
}
