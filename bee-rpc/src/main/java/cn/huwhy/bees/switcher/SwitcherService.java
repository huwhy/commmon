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

package cn.huwhy.bees.switcher;

import cn.huwhy.bees.core.extension.Scope;
import cn.huwhy.bees.core.extension.Spi;

/**
 * 
 * @author maijunsheng
 * @version 创建时间：2013-6-14
 * @author zhanglei
 * 
 */
@Spi(scope = Scope.SINGLETON)
public interface SwitcherService {
    /**
     * 获取接口降级开关
     * 
     * @return
     */
    Switcher getSwitcher();

    /**
     * 初始化开关。
     *
     * @param initialValue
     */
    void initSwitcher(boolean initialValue);

    /**
     * 检查开关是否开启。
     * 
     * @return true ：设置来开关，并且开关值为true false：未设置开关或开关为false
     */
    boolean isOpen();

    /**
     * 检查开关是否开启，如果开关不存在则将开关置默认值，并返回。
     * 
     * @param defaultValue
     * @return 开关存在时返回开关值，开关不存在时设置开关为默认值，并返回默认值。
     */
    boolean isOpen(boolean defaultValue);

    /**
     * 设置开关状态。
     * 
     * @param value
     */
    void setValue(boolean value);

    /**
     * register a listener for switcher value change, register a listener twice will only fire once
     * 
     * @param listener
     */
    void registerListener(SwitcherListener listener);

    /**
     * unregister a listener
     * 
     * @param listener the listener to be unregistered, null for all listeners for this switcherName
     */
    void unRegisterListener(SwitcherListener listener);

}
