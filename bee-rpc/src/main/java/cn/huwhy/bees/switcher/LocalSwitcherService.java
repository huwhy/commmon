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

import java.util.ArrayList;
import java.util.List;

import cn.huwhy.bees.core.extension.SpiMeta;

/**
 * @author maijunsheng
 * @version 创建时间：2013-6-14
 */
@SpiMeta(name = "localSwitcherService")
public class LocalSwitcherService implements SwitcherService {

    private Switcher switcher;

    private List<SwitcherListener> listeners = new ArrayList<>();

    @Override
    public Switcher getSwitcher() {
        return switcher;
    }

    @Override
    public void initSwitcher(boolean initialValue) {
        setValue(initialValue);
    }

    @Override
    public boolean isOpen() {
        return switcher != null && switcher.isOn();
    }

    @Override
    public boolean isOpen(boolean defaultValue) {
        if (switcher == null) {
            switcher = new Switcher(defaultValue);
        }
        return switcher.isOn();
    }

    @Override
    public void setValue(boolean value) {
        switcher = new Switcher(value);
        for (SwitcherListener listener : listeners) {
            listener.onValueChanged(value);
        }
    }

    @Override
    public void registerListener(SwitcherListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    @Override
    public void unRegisterListener(SwitcherListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

}
