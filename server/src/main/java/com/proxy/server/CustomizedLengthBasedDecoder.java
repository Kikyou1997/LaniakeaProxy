package com.proxy.server;

import base.constants.Packets;
import base.constants.RequestCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author kikyou
 * Created at 2020/2/18
 */
public class CustomizedLengthBasedDecoder extends LengthFieldBasedFrameDecoder {

    public CustomizedLengthBasedDecoder() {
        super(Integer.MAX_VALUE, Packets.FIELD_CODE_LENGTH + Packets.FIELD_ID_LENGTH, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in.readByte() == RequestCode.CONNECT) {
            ctx.fireChannelRead(in);
            return in;
        }

        return super.decode(ctx, in);
    }
}
