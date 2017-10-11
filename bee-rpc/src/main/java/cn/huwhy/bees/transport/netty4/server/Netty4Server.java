package cn.huwhy.bees.transport.netty4.server;

import cn.huwhy.bees.common.ChannelState;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.rpc.Request;
import cn.huwhy.bees.rpc.Response;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.transport.AbstractServer;
import cn.huwhy.bees.transport.MessageHandler;
import cn.huwhy.bees.transport.TransportException;
import cn.huwhy.bees.util.LoggerUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Netty4Server extends AbstractServer {

    protected volatile ChannelState state = ChannelState.UNINIT;

    private ServerBootstrap bootstrap = new ServerBootstrap();

    private MessageHandler messageHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Channel serverChannel;

    private Netty4ServerInitializer serverInitializer;

    public Netty4Server(URL url, MessageHandler messageHandler) {
        super(url);
        this.messageHandler = messageHandler;
    }

    @Override
    public boolean isBound() {
        return serverChannel != null && serverChannel.isActive();
    }

    @Override
    public Response request(Request request) throws TransportException {
        throw new BeesFrameworkException("NettyServer request(Request request) method unsupport: url: " + url);
    }

    @Override
    public boolean open() {
        if (isAvailable()) {
            LoggerUtil.debug("NettyServer ServerChannel already Open: url=" + url);
            return true;
        }

        LoggerUtil.info("NettyServer ServerChannel start to Open: url=" + url);

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    initServerBootstrap();

                    serverChannel = bootstrap.bind(url.getPort()).sync().channel();
                    state = ChannelState.ALIVE;
                    LoggerUtil.info("NettyServer ServerChannel finish Open: url=" + url);
                    serverChannel.closeFuture().sync();
                } catch (InterruptedException e) {
                    LoggerUtil.error("NettyServer open interrupted: url=" + url.getUri(), e);
                } finally {
                    close();
                }
            }
        }).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LoggerUtil.error("NettyServer open interrupted: url=" + url.getUri(), e);
        }

        return state.isAliveState();
    }

    private synchronized void initServerBootstrap() {
        bootstrap = new ServerBootstrap();

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        serverInitializer = new Netty4ServerInitializer(this, url, codec, messageHandler);
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(serverInitializer);

        bootstrap.childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    @Override
    public void close() {
        close(0);
    }

    @Override
    public void close(int timeout) {
        if (state.isCloseState()) {
            LoggerUtil.info("NettyServer close fail: already close, url={}", url.getUri());
            return;
        }

        if (state.isUnInitState()) {
            LoggerUtil.info("NettyServer close Fail: don't need to close because node is unInit state: url={}",
                    url.getUri());
            return;
        }

        try {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serverChannel.close();
            serverInitializer.close();
            state = ChannelState.CLOSE;
            LoggerUtil.info("NettyServer close Success: url={}", url.getUri());
        } catch (Exception e) {
            LoggerUtil.error("NettyServer close Error: url=" + url.getUri(), e);
        }
    }

    @Override
    public boolean isClosed() {
        return state.isCloseState();
    }

    @Override
    public boolean isAvailable() {
        return state.isAliveState();
    }

    @Override
    public URL getUrl() {
        return url;
    }

}
