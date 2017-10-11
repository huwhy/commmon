package cn.huwhy.bees.transport.netty4;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import cn.huwhy.bees.codec.Codec;
import cn.huwhy.bees.common.BeesConstants;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.exception.BeesServiceException;
import cn.huwhy.bees.rpc.DefaultResponse;
import cn.huwhy.bees.rpc.Response;
import cn.huwhy.bees.transport.Channel;
import cn.huwhy.bees.util.LoggerUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class Netty4Decoder extends ByteToMessageDecoder {

    private Codec                                 codec;
    private Channel client;
    private int maxContentLength = 0;

    public Netty4Decoder(Codec codec, Channel client, int maxContentLength) {
        this.codec = codec;
        this.client = client;
        this.maxContentLength = maxContentLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        io.netty.channel.Channel channel = ctx.channel();

        if (byteBuf.readableBytes() <= BeesConstants.NETTY_HEADER) {
            return;
        }

        byteBuf.markReaderIndex();

        short type = byteBuf.readShort();

        if (type != BeesConstants.NETTY_MAGIC_TYPE) {
            byteBuf.resetReaderIndex();
            throw new BeesFrameworkException("NettyDecoder transport header not support, type: " + type);
        }

        byte messageType = (byte) byteBuf.readShort();
        long requestId = byteBuf.readLong();

        int dataLength = byteBuf.readInt();

        // FIXME 如果dataLength过大，可能导致问题
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }

        if (maxContentLength > 0 && dataLength > maxContentLength) {
            LoggerUtil.warn(
                    "NettyDecoder transport data content length over of limit, size: {}  > {}. remote={} local={}",
                    dataLength, maxContentLength, channel.remoteAddress(), channel.localAddress());
            Exception e = new BeesServiceException("NettyDecoder transport data content length over of limit, size: "
                    + dataLength + " > " + maxContentLength);

            if (messageType == BeesConstants.FLAG_REQUEST) {
                Response response = buildExceptionResponse(requestId, e);
                channel.write(response);
                throw e;
            } else {
                throw e;
            }
        }

        // TODO use byte array pool
        byte[] data = new byte[dataLength];

        byteBuf.readBytes(data);

        try {
            String remoteIp = getRemoteIp(channel);
            out.add(codec.decode(client, remoteIp, data));
        } catch (Exception e) {
            if (messageType == BeesConstants.FLAG_REQUEST) {
                Response response = buildExceptionResponse(requestId, e);
                channel.write(response);
                out.add(response);
            } else {
                out.add(buildExceptionResponse(requestId, e));
            }
        }

    }


    private Response buildExceptionResponse(long requestId, Exception e) {
        DefaultResponse response = new DefaultResponse();
        response.setRequestId(requestId);
        response.setException(e);
        return response;
    }


    private String getRemoteIp(io.netty.channel.Channel channel) {
        String ip = "";
        SocketAddress remote = channel.remoteAddress();
        if (remote != null) {
            try {
                ip = ((InetSocketAddress) remote).getAddress().getHostAddress();
            } catch (Exception e) {
                LoggerUtil.warn("get remoteIp error!dedault will use. msg:" + e.getMessage() + ", remote:" + remote.toString());
            }
        }
        return ip;
    }

}
