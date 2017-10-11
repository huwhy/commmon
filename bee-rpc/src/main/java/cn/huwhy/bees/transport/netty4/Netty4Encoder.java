package cn.huwhy.bees.transport.netty4;

import cn.huwhy.bees.codec.Codec;
import cn.huwhy.bees.common.BeesConstants;
import cn.huwhy.bees.rpc.DefaultResponse;
import cn.huwhy.bees.rpc.Request;
import cn.huwhy.bees.rpc.Response;
import cn.huwhy.bees.transport.Channel;
import cn.huwhy.bees.util.ByteUtil;
import cn.huwhy.bees.util.LoggerUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Netty4Encoder extends MessageToByteEncoder<Object> {

    private Codec   codec;
    private Channel client;

    public Netty4Encoder(Codec codec, Channel client) {
        this.codec = codec;
        this.client = client;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object message, ByteBuf out) throws Exception {

        long requestId = getRequestId(message);
        byte[] data = null;

        if (message instanceof Response) {
            try {
                data = codec.encode(client, message);
            } catch (Exception e) {
                LoggerUtil.error("NettyEncoder encode error, identity=" + client.getUrl().getIdentity(), e);
                Response response = buildExceptionResponse(requestId, e);
                data = codec.encode(client, response);
            }
        } else {
            data = codec.encode(client, message);
        }

        byte[] transportHeader = new byte[BeesConstants.NETTY_HEADER];
        ByteUtil.short2bytes(BeesConstants.NETTY_MAGIC_TYPE, transportHeader, 0);
        transportHeader[3] = getType(message);
        ByteUtil.long2bytes(getRequestId(message), transportHeader, 4);
        ByteUtil.int2bytes(data.length, transportHeader, 12);

        out.writeBytes(transportHeader);
        out.writeBytes(data);
    }


    private long getRequestId(Object message) {
        if (message instanceof Request) {
            return ((Request) message).getRequestId();
        } else if (message instanceof Response) {
            return ((Response) message).getRequestId();
        } else {
            return 0;
        }
    }

    private byte getType(Object message) {
        if (message instanceof Request) {
            return BeesConstants.FLAG_REQUEST;
        } else if (message instanceof Response) {
            return BeesConstants.FLAG_RESPONSE;
        } else {
            return BeesConstants.FLAG_OTHER;
        }
    }

    private Response buildExceptionResponse(long requestId, Exception e) {
        DefaultResponse response = new DefaultResponse();
        response.setRequestId(requestId);
        response.setException(e);
        return response;
    }
}
