package cn.huwhy.bees.transport.netty4;

import cn.huwhy.bees.core.extension.SpiMeta;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.transport.Client;
import cn.huwhy.bees.transport.MessageHandler;
import cn.huwhy.bees.transport.Server;
import cn.huwhy.bees.transport.netty4.client.Netty4Client;
import cn.huwhy.bees.transport.netty4.server.Netty4Server;
import cn.huwhy.bees.transport.support.AbstractEndpointFactory;

@SpiMeta(name = "bees")
public class Netty4EndpointFactory extends AbstractEndpointFactory {

	@Override
	protected Server innerCreateServer(URL url, MessageHandler messageHandler) {
		return new Netty4Server(url, messageHandler);
	}

	@Override
	protected Client innerCreateClient(URL url) {
		return new Netty4Client(url);
	}

}
