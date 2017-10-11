package cn.huwhy.bees.transport.netty4.client;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import cn.huwhy.bees.common.ChannelState;
import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.exception.BeesErrorMsgConstant;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.exception.BeesServiceException;
import cn.huwhy.bees.rpc.Future;
import cn.huwhy.bees.rpc.FutureListener;
import cn.huwhy.bees.rpc.Request;
import cn.huwhy.bees.rpc.Response;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.transport.Channel;
import cn.huwhy.bees.transport.TransportException;
import cn.huwhy.bees.util.ExceptionUtil;
import cn.huwhy.bees.util.LoggerUtil;
import cn.huwhy.bees.util.BeesFrameworkUtil;
import io.netty.channel.ChannelFuture;

/**
 * @author maijunsheng
 * @version 创建时间：2013-5-31
 */
public class NettyChannel implements Channel {
    private volatile ChannelState state = ChannelState.UNINIT;

    private Netty4Client nettyClient;

    private io.netty.channel.Channel channel = null;

    private InetSocketAddress remoteAddress = null;
    private InetSocketAddress localAddress  = null;

    public NettyChannel(Netty4Client nettyClient) {
        this.nettyClient = nettyClient;
        this.remoteAddress = new InetSocketAddress(nettyClient.getUrl().getHost(), nettyClient.getUrl().getPort());
    }

    @Override
    public Response request(Request request) throws TransportException {
        int timeout = nettyClient.getUrl().getMethodParameter(request.getMethodName(), request.getParamtersDesc(),
                URLParamType.requestTimeout.getName(), URLParamType.requestTimeout.getIntValue());
        if (timeout <= 0) {
            throw new BeesFrameworkException("Netty4Client init Error: timeout(" + timeout + ") <= 0 is forbid.",
                    BeesErrorMsgConstant.FRAMEWORK_INIT_ERROR);
        }
        NettyResponseFuture response = new NettyResponseFuture(request, timeout, this.nettyClient);
        this.nettyClient.registerCallback(request.getRequestId(), response);

        ChannelFuture writeFuture = this.channel.writeAndFlush(request);

        boolean result = writeFuture.awaitUninterruptibly(timeout, TimeUnit.SECONDS);

        if (result && writeFuture.isSuccess()) {
            response.addListener(new FutureListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    if (future.isSuccess() || (future.isDone() && ExceptionUtil.isBizException(future.getException()))) {
                        // 成功的调用
                        nettyClient.resetErrorCount();
                    } else {
                        // 失败的调用
                        nettyClient.incrErrorCount();
                    }
                }
            });
            return response;
        }

        writeFuture.cancel(true);
        response = this.nettyClient.removeCallback(request.getRequestId());

        if (response != null) {
            response.cancel();
        }

        // 失败的调用
        nettyClient.incrErrorCount();

        if (writeFuture.cause() != null) {
            throw new BeesServiceException("NettyChannel send request to server Error: url="
                    + nettyClient.getUrl().getUri() + " local=" + localAddress + " "
                    + BeesFrameworkUtil.toString(request), writeFuture.cause());
        } else {
            throw new BeesServiceException("NettyChannel send request to server Timeout: url="
                    + nettyClient.getUrl().getUri() + " local=" + localAddress + " "
                    + BeesFrameworkUtil.toString(request));
        }
    }

    @Override
    public synchronized boolean open() {
        if (isAvailable()) {
            LoggerUtil.warn("the channel already open, local: " + localAddress + " remote: " + remoteAddress + " url: "
                    + nettyClient.getUrl().getUri());
            return true;
        }

        try {
            ChannelFuture channelFuture = nettyClient.getBootstrap().connect(
                    new InetSocketAddress(nettyClient.getUrl().getHost(), nettyClient.getUrl().getPort()));

            long start = System.currentTimeMillis();

            int timeout = nettyClient.getUrl().getIntParameter(URLParamType.connectTimeout.getName(), URLParamType.connectTimeout.getIntValue());
            if (timeout <= 0) {
                throw new BeesFrameworkException("Netty4Client init Error: timeout(" + timeout + ") <= 0 is forbid.",
                        BeesErrorMsgConstant.FRAMEWORK_INIT_ERROR);
            }
            // 不去依赖于connectTimeout
            boolean result = channelFuture.awaitUninterruptibly(timeout, TimeUnit.MILLISECONDS);
            boolean success = channelFuture.isSuccess();

            if (result && success) {
                channel = channelFuture.channel();
                if (channel.localAddress() != null && channel.localAddress() instanceof InetSocketAddress) {
                    localAddress = (InetSocketAddress) channel.localAddress();
                }

                state = ChannelState.ALIVE;
                return true;
            }
            boolean connected = false;
            if (channelFuture.channel() != null) {
                connected = channelFuture.channel().isOpen();
            }

            if (channelFuture.cause() != null) {
                channelFuture.cancel(true);
                throw new BeesServiceException("NettyChannel failed to connect to server, url: "
                        + nettyClient.getUrl().getUri() + ", result: " + result + ", success: " + success + ", connected: " + connected, channelFuture.cause());
            } else {
                channelFuture.cancel(true);
                throw new BeesServiceException("NettyChannel connect to server timeout url: "
                        + nettyClient.getUrl().getUri() + ", cost: " + (System.currentTimeMillis() - start) + ", result: " + result + ", success: " + success + ", connected: " + connected);
            }
        } catch (BeesServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new BeesServiceException("NettyChannel failed to connect to server, url: "
                    + nettyClient.getUrl().getUri(), e);
        } finally {
            if (!state.isAliveState()) {
                nettyClient.incrErrorCount();
            }
        }
    }

    @Override
    public synchronized void close() {
        close(0);
    }

    @Override
    public synchronized void close(int timeout) {
        try {
            if (channel != null) {
                channel.close();
            }

            state = ChannelState.CLOSE;
        } catch (Exception e) {
            LoggerUtil
                    .error("NettyChannel close Error: " + nettyClient.getUrl().getUri() + " local=" + localAddress, e);
        }
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
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
        return nettyClient.getUrl();
    }
}
