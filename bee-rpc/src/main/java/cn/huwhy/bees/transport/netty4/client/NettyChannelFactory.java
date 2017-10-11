package cn.huwhy.bees.transport.netty4.client;

import org.apache.commons.pool.BasePoolableObjectFactory;

import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.util.LoggerUtil;

/**
 * @author maijunsheng
 * @version 创建时间：2013-5-31
 * 
 */
public class NettyChannelFactory extends BasePoolableObjectFactory {
	private String factoryName = "";
	private Netty4Client nettyClient;

	public NettyChannelFactory(Netty4Client nettyClient) {
		super();

		this.nettyClient = nettyClient;
		this.factoryName = "NettyChannelFactory_" + nettyClient.getUrl().getHost() + "_"
				+ nettyClient.getUrl().getPort();
	}

	public String getFactoryName() {
		return factoryName;
	}

	@Override
	public String toString() {
		return factoryName;
	}

	@Override
	public Object makeObject() throws Exception {
		NettyChannel nettyChannel = new NettyChannel(nettyClient);
		nettyChannel.open();

		return nettyChannel;
	}

	@Override
	public void destroyObject(final Object obj) throws Exception {
		if (obj instanceof NettyChannel) {
			NettyChannel client = (NettyChannel) obj;
			URL url = nettyClient.getUrl();

			try {
				client.close();

				LoggerUtil.info(factoryName + " client disconnect Success: " + url.getUri());
			} catch (Exception e) {
				LoggerUtil.error(factoryName + " client disconnect Error: " + url.getUri(), e);
			}
		}
	}

	@Override
	public boolean validateObject(final Object obj) {
		if (obj instanceof NettyChannel) {
			final NettyChannel client = (NettyChannel) obj;
			try {
				return client.isAvailable();
			} catch (final Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public void activateObject(Object obj) throws Exception {
		if (obj instanceof NettyChannel) {
			final NettyChannel client = (NettyChannel) obj;
			if (!client.isAvailable()) {
				client.open();
			}
		}
	}

	@Override
	public void passivateObject(Object obj) throws Exception {
	}
}
