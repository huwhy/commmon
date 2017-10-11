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

import cn.huwhy.bees.registry.Registry;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.core.extension.SpiMeta;

/**
 * Created by axb on 16/6/12.
 */
@SpiMeta(name = "direct")
public class DirectRegistryFactory extends AbstractRegistryFactory {

    @Override
    protected Registry createRegistry(URL url) {
        return new DirectRegistry(url);
    }


}
