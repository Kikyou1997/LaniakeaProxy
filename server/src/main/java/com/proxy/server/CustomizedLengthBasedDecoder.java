package com.proxy.server;

import base.constants.Packets;
import base.constants.RequestCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * Created at 2020/2/18
 */
@Slf4j
public class CustomizedLengthBasedDecoder extends LengthFieldBasedFrameDecoder {
    private static int fieldOffset = Packets.FIELD_CODE_LENGTH + Packets.FIELD_ID_LENGTH;

    // 默认按照 packets_total_size - lengthFieldOffset - lengthFieldLength进行统计
    // 也就是说当 cumulated = packets_total_size - lengthFieldOffset - lengthFieldLength时 完成解码 交给下一个
    public CustomizedLengthBasedDecoder() {
        super(Integer.MAX_VALUE, 5, 4);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte code = ((ByteBuf)msg).readByte();
        ((ByteBuf) msg).readerIndex(0);
        if (code != RequestCode.DATA_TRANS_REQ){
            ctx.fireChannelRead(msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
