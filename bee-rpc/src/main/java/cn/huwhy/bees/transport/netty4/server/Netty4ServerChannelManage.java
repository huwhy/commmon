package cn.huwhy.bees.transport.netty4.server;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.huwhy.bees.util.LoggerUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public class Netty4ServerChannelManage extends ChannelInboundHandlerAdapter {
    private ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    private int maxChannel = 0;

    public Netty4ServerChannelManage(int maxChannel) {
        super();
        this.maxChannel = maxChannel;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

        String channelKey = getChannelKey((InetSocketAddress) channel.localAddress(),
                (InetSocketAddress) channel.remoteAddress());

        if (channels.size() > maxChannel) {
            // 超过最大连接数限制，直接close连接
            LoggerUtil.warn("Netty4ServerChannelManage channelConnected channel size out of limit: limit={} current={}",
                    maxChannel, channels.size());

            channel.close();
        } else {
            channels.put(channelKey, channel);
            ctx.fireChannelRegistered();
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

        String channelKey = getChannelKey((InetSocketAddress) channel.localAddress(),
                (InetSocketAddress) channel.remoteAddress());

        channels.remove(channelKey);
        ctx.fireChannelUnregistered();
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    /**
     * close所有的连接
     */
    public void close() {
        for (Channel ch : channels.values()) {
            try {
                if (ch != null) {
                    ch.close();
                }
            } catch (Exception e) {
                LoggerUtil.error("Netty4ServerChannelManage close channel Error: "
                        + getChannelKey((InetSocketAddress) ch.localAddress(), (InetSocketAddress) ch.remoteAddress()), e);
            }
        }
    }

    /**
     * remote address + local address 作为连接的唯一标示
     *
     * @param local
     * @param remote
     * @return
     */
    private String getChannelKey(InetSocketAddress local, InetSocketAddress remote) {
        String key = "";
        if (local == null || local.getAddress() == null) {
            key += "null-";
        } else {
            key += local.getAddress().getHostAddress() + ":" + local.getPort() + "-";
        }

        if (remote == null || remote.getAddress() == null) {
            key += "null";
        } else {
            key += remote.getAddress().getHostAddress() + ":" + remote.getPort();
        }

        return key;
    }
}