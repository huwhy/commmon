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

package cn.huwhy.bees.transport.support;

import cn.huwhy.bees.rpc.DefaultResponse;
import cn.huwhy.bees.common.BeesConstants;
import cn.huwhy.bees.core.extension.SpiMeta;
import cn.huwhy.bees.rpc.DefaultRequest;
import cn.huwhy.bees.rpc.Request;
import cn.huwhy.bees.transport.Channel;
import cn.huwhy.bees.transport.HeartbeatFactory;
import cn.huwhy.bees.transport.MessageHandler;
import cn.huwhy.bees.util.RequestIdGenerator;

/**
 * @author maijunsheng
 * @version 创建时间：2013-6-14
 * 
 */
@SpiMeta(name = "bees")
public class DefaultRpcHeartbeatFactory implements HeartbeatFactory {

    @Override
    public Request createRequest() {
        DefaultRequest request = new DefaultRequest();

        request.setRequestId(RequestIdGenerator.getRequestId());
        request.setInterfaceName(BeesConstants.HEARTBEAT_INTERFACE_NAME);
        request.setMethodName(BeesConstants.HEARTBEAT_METHOD_NAME);
        request.setParamtersDesc(BeesConstants.HHEARTBEAT_PARAM);

        return request;
    }

    @Override
    public MessageHandler wrapMessageHandler(MessageHandler handler) {
        return new HeartMessageHandleWrapper(handler);
    }

    public static boolean isHeartbeatRequest(Object message) {
        if (!(message instanceof Request)) {
            return false;
        }

        Request request = (Request) message;

        return BeesConstants.HEARTBEAT_INTERFACE_NAME.equals(request.getInterfaceName())
                && BeesConstants.HEARTBEAT_METHOD_NAME.equals(request.getMethodName())
                && BeesConstants.HHEARTBEAT_PARAM.endsWith(request.getParamtersDesc());
    }


    private class HeartMessageHandleWrapper implements MessageHandler {
        private MessageHandler messageHandler;

        public HeartMessageHandleWrapper(MessageHandler messageHandler) {
            this.messageHandler = messageHandler;
        }

        @Override
        public Object handle(Channel channel, Object message) {
            if (isHeartbeatRequest(message)) {
                DefaultResponse response = new DefaultResponse();
                response.setValue("heartbeat");
                return response;
            }

            return messageHandler.handle(channel, message);
        }


    }
}
