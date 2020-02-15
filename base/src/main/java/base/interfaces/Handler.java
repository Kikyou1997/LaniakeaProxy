package base.interfaces;

import base.constants.Packets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author kikyou
 * Created at 2020/1/30
 */
public interface Handler<R> {

    R handle(Object msg, ChannelHandlerContext ctx) throws Exception;

    default boolean isProxyMessage(ByteBuf buf) {
        buf.readerIndex(0);
        return buf.readShort() == Packets.MAGIC;
    }
}
