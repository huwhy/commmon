package cn.huwhy.bees.transport.netty4.server;

import cn.huwhy.bees.codec.Codec;
import cn.huwhy.bees.common.BeesConstants;
import cn.huwhy.bees.common.URLParamType;
import cn.huwhy.bees.core.DefaultThreadFactory;
import cn.huwhy.bees.core.extension.ExtensionLoader;
import cn.huwhy.bees.rpc.URL;
import cn.huwhy.bees.transport.Channel;
import cn.huwhy.bees.transport.MessageHandler;
import cn.huwhy.bees.transport.RequestIdempotentManager;
import cn.huwhy.bees.transport.netty4.Netty4Decoder;
import cn.huwhy.bees.transport.netty4.Netty4Encoder;
import cn.huwhy.bees.transport.netty4.StandardThreadExecutor;
import cn.huwhy.bees.util.StatisticCallback;
import cn.huwhy.bees.util.StatsUtil;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class Netty4ServerInitializer extends ChannelInitializer<SocketChannel> implements StatisticCallback {

    private URL url;

    private Codec codec;

    private Channel client;

    private Netty4ServerChannelManage channelManage;

    private StandardThreadExecutor executor;

    private Netty4ServerHandler serverHandler;

    public Netty4ServerInitializer(Channel client, URL url, Codec codec, MessageHandler messageHandler) {
        this.url = url;
        this.codec = codec;
        this.client = client;

        // 连接数的管理，进行最大连接数的限制
        int maxServerConnection = url.getIntParameter(URLParamType.maxServerConnection.getName(),
                URLParamType.maxServerConnection.getIntValue());
        channelManage = new Netty4ServerChannelManage(maxServerConnection);

        executor = initExecutor();

        String idempotentName = url.getParameter(URLParamType.idempotent.getName());
        if (URLParamType.idempotent.getValue().equals(idempotentName)) {
            RequestIdempotentManager requestIdempotentManager = ExtensionLoader.getExtensionLoader(RequestIdempotentManager.class).getExtension(idempotentName);
            serverHandler = new Netty4ServerHandler(executor, messageHandler, client, requestIdempotentManager);
        } else {
            serverHandler = new Netty4ServerHandler(executor, messageHandler, client, idempotentName);
        }
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        p.addLast("channel_manage", channelManage);

        int maxContentLength = url.getIntParameter(URLParamType.maxContentLength.getName(),
                URLParamType.maxContentLength.getIntValue());
        p.addLast("decoder", new Netty4Decoder(codec, client, maxContentLength))
                .addLast("encoder", new Netty4Encoder(codec, client));

        p.addLast("handler", serverHandler);

        StatsUtil.registryStatisticCallback(this);
    }

    private StandardThreadExecutor initExecutor() {
        boolean shareChannel = url.getBooleanParameter(URLParamType.shareChannel.getName(),
                URLParamType.shareChannel.getBooleanValue());
        int workerQueueSize = url.getIntParameter(URLParamType.workerQueueSize.getName(),
                URLParamType.workerQueueSize.getIntValue());

        int minWorkerThread, maxWorkerThread;

        if (shareChannel) {
            minWorkerThread = url.getIntParameter(URLParamType.minWorkerThread.getName(),
                    BeesConstants.NETTY_SHARECHANNEL_MIN_WORKDER);
            maxWorkerThread = url.getIntParameter(URLParamType.maxWorkerThread.getName(),
                    BeesConstants.NETTY_SHARECHANNEL_MAX_WORKDER);
        } else {
            minWorkerThread = url.getIntParameter(URLParamType.minWorkerThread.getName(),
                    BeesConstants.NETTY_NOT_SHARECHANNEL_MIN_WORKDER);
            maxWorkerThread = url.getIntParameter(URLParamType.maxWorkerThread.getName(),
                    BeesConstants.NETTY_NOT_SHARECHANNEL_MAX_WORKDER);
        }

        StandardThreadExecutor executor = new StandardThreadExecutor(minWorkerThread, maxWorkerThread, workerQueueSize,
                new DefaultThreadFactory("NettyServer-" + url.getServerPortStr(), true));
        executor.prestartAllCoreThreads();
        return executor;
    }

    public void close() {
        channelManage.close();
        executor.shutdown();
        StatsUtil.unRegistryStatisticCallback(this);
    }

    @Override
    public String statisticCallback() {
        return String.format(
                "identity: %s connectionCount: %s taskCount: %s queueCount: %s maxThreadCount: %s maxTaskCount: %s",
                url.getIdentity(), channelManage.getChannels().size(), executor.getSubmittedTasksCount(),
                executor.getQueue().size(), executor.getMaximumPoolSize(),
                executor.getMaxSubmittedTaskCount());
    }
}
