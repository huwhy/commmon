package cn.huwhy.bees.transport.netty4.server;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import cn.huwhy.bees.common.RequestState;
import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.config.springsupport.util.SpringContentUtil;
import cn.huwhy.bees.exception.BeesDoingException;
import cn.huwhy.bees.exception.BeesErrorMsgConstant;
import cn.huwhy.bees.exception.BeesServiceException;
import cn.huwhy.bees.rpc.DefaultResponse;
import cn.huwhy.bees.rpc.Request;
import cn.huwhy.bees.transport.Channel;
import cn.huwhy.bees.transport.MessageHandler;
import cn.huwhy.bees.transport.RequestIdempotentManager;
import cn.huwhy.bees.util.LoggerUtil;
import cn.huwhy.bees.util.NetUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class Netty4ServerHandler extends SimpleChannelInboundHandler<Request> {

    private ThreadPoolExecutor       executor;
    private MessageHandler           messageHandler;
    private Channel                  serverChannel;
    private RequestIdempotentManager requestIdempotentManager;
    private String                   idempotentName;

    public Netty4ServerHandler(ThreadPoolExecutor executor, MessageHandler messageHandler, Channel serverChannel, String idempotentName) {
        this.executor = executor;
        this.messageHandler = messageHandler;
        this.serverChannel = serverChannel;
        this.idempotentName = idempotentName;
    }

    public Netty4ServerHandler(ThreadPoolExecutor executor, MessageHandler messageHandler, Channel serverChannel, RequestIdempotentManager idempotentManager) {
        this.executor = executor;
        this.messageHandler = messageHandler;
        this.serverChannel = serverChannel;
        this.requestIdempotentManager = idempotentManager;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        LoggerUtil.info("NettyChannelHandler channelRegistered: remote=" + ctx.channel().remoteAddress()
                + " local=" + ctx.channel().localAddress());

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LoggerUtil.info("NettyChannelHandler channelDisconnected: remote=" + ctx.channel().remoteAddress()
                + " local=" + ctx.channel().localAddress());

    }

    /**
     * <pre>
     *  request process: 主要来自于client的请求，需要使用threadPoolExecutor进行处理，避免service message处理比较慢导致iothread被阻塞
     * </pre>
     *
     * @param ctx
     * @param request
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Request request) throws Exception {
        request.setAttachment(URLParamType.host.getName(), NetUtils.getHostName(ctx.channel().remoteAddress()));
        if (requestIdempotentManager == null && idempotentName != null) {
            requestIdempotentManager = SpringContentUtil.getBean(idempotentName, RequestIdempotentManager.class);
        }
        final long processStartTime = System.currentTimeMillis();

        // 使用线程池方式处理
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    processRequest(ctx, request, processStartTime);
                }
            });
        } catch (RejectedExecutionException rejectException) {
            DefaultResponse response = new DefaultResponse();
            response.setRequestId(request.getRequestId());
            response.setException(new BeesServiceException("process thread pool is full, reject",
                    BeesErrorMsgConstant.SERVICE_REJECT));
            response.setProcessTime(System.currentTimeMillis() - processStartTime);
            ctx.channel().writeAndFlush(response);

            LoggerUtil
                    .debug("process thread pool is full, reject, active={} poolSize={} corePoolSize={} maxPoolSize={} taskCount={} requestId={}",
                            executor.getActiveCount(), executor.getPoolSize(),
                            executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                            executor.getTaskCount(), request.getRequestId());
        }
    }

    private void processRequest(ChannelHandlerContext ctx, Request request, long processStartTime) {
        RequestState state = requestIdempotentManager.state(request);
        DefaultResponse response;
        Object result;
        if (RequestState.DOING.equals(state)) {
            LoggerUtil.debug("request in doing: {} - {}.{}", request.getRequestId(), request.getInterfaceName(), request.getMethodName());
            response = new DefaultResponse();
            response.setException(new BeesDoingException());
        } else if (RequestState.DONE.equals(state)) {
            LoggerUtil.debug("request is done: {} - {}.{}", request.getRequestId(), request.getInterfaceName(), request.getMethodName());
            response = requestIdempotentManager.getResult(request);
        } else {
            LoggerUtil.debug("request is doing: {} - {}.{}", request.getRequestId(), request.getInterfaceName(), request.getMethodName());
            requestIdempotentManager.doing(request);
            result = messageHandler.handle(serverChannel, request);
            if (!(result instanceof DefaultResponse)) {
                response = new DefaultResponse(result);
            } else {
                response = (DefaultResponse) result;
            }
        }
        response.setRequestId(request.getRequestId());
        response.setProcessTime(System.currentTimeMillis() - processStartTime);

        if (ctx.channel().isOpen()) {
            ctx.channel().writeAndFlush(response);
            requestIdempotentManager.clear(request);
        } else if (RequestState.NONE.equals(state) || RequestState.DOING.equals(state)) {
            LoggerUtil.debug("request done channel close: {} - {}, cost: {}", request.getRequestId(), request.getInterfaceName(), response.getProcessTime());
            requestIdempotentManager.done(request, response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        LoggerUtil.error("NettyChannelHandler exceptionCaught: remote=" + ctx.channel().remoteAddress()
                + " local=" + ctx.channel().localAddress() + " event=" + e.getCause(), e.getCause());

        ctx.channel().close();
    }
}
