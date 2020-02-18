package com.proxy.server;

import static base.constants.RequestCode.*;

import base.AuthImpl;
import base.CryptoImpl;
import base.MessageGenerator;
import base.constants.Packets;
import base.interfaces.Handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 只用于代理服务端 对来自客户端的请求的处理
 *
 * @author kikyou
 * Created at 2020/2/1
 */
@ChannelHandler.Sharable
public class MessageProcessor extends SimpleChannelInboundHandler<ByteBuf> {

    private Map<Byte, Handler> byteHandlerMap = new ConcurrentHashMap<>();
    private static final int HEADER_LENGTH =  Packets.FIELD_CODE_LENGTH + Packets.FIELD_ID_LENGTH + Packets.FIELD_LENGTH_LEN;

    {
        byteHandlerMap.put(AUTH_REQ, new AuthImpl());
        byteHandlerMap.put(DATA_TRANS_REQ, new CryptoImpl());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte requestCode = getRequestCode(msg);
        Handler handler = null;
        switch (requestCode) {
            case GET_CLOCK_REQ:
                ByteBuf buf = MessageGenerator.generateClockResponse();
                ctx.channel().writeAndFlush(buf);
                ReferenceCountUtil.release(buf);
                return;
            case AUTH_REQ:
                handler = byteHandlerMap.get(AUTH_REQ);
                handler.handle(msg, ctx);
                return;
        }
        removeRedundantHeader(msg);
        ctx.fireChannelRead(msg);
    }

    public static byte getRequestCode(ByteBuf msg) {
        return msg.readByte();
    }

    private void removeRedundantHeader(ByteBuf buf) {
        buf.readerIndex(HEADER_LENGTH);
        byte[] msg = new byte[buf.readableBytes()];
        buf.readBytes(msg);
        buf.clear();
        buf.writeBytes(msg);
    }

}
