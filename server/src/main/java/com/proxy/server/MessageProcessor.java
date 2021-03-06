package com.proxy.server;

import static base.constants.RequestCode.*;

import base.arch.MessageGenerator;
import base.constants.Packets;
import base.interfaces.Handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 只用于代理服务端 对来自客户端的请求的处理
 *
 * @author kikyou
 * Created at 2020/2/1
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageProcessor extends ChannelInboundHandlerAdapter {

    private Map<Byte, Handler> byteHandlerMap = new ConcurrentHashMap<>();
    private static final int HEADER_LENGTH = Packets.FIELD_CODE_LENGTH + Packets.FIELD_ID_LENGTH + Packets.FIELD_LENGTH_LEN;

    {
        byteHandlerMap.put(AUTH_REQ, new AuthImpl());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        var msg = (ByteBuf)obj;
        byte requestCode = getRequestCode(msg);
        Handler handler = null;
        switch (requestCode) {
            case GET_CLOCK_REQ:
                ByteBuf buf = MessageGenerator.generateClockResponse();
                ctx.channel().writeAndFlush(buf);
                return;
            case AUTH_REQ:
                handler = byteHandlerMap.get(AUTH_REQ);
                handler.handle(msg, ctx);
                return;
            case GET_USED_TRAFFIC:
        }
        msg.readerIndex(0);
        ctx.fireChannelRead(msg);
    }

    public static byte getRequestCode(ByteBuf msg) {
        return msg.readByte();
    }

}
