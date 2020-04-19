package base.arch;

import base.protocol.LaniakeaPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import base.constants.ResponseCode;

import java.util.List;

/**
 * 为确保bytebuf的字节数足够凑成一个新的应用层报文，应该将此Decoder置于LengthFieldBasedDecoder之后
 */
public class DataTransmissionPacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        LaniakeaPacket packet = new LaniakeaPacket();
        byte code = in.readByte();
        packet.setCode(code);

        int id = in.readInt();
        int length = in.readInt();
        ByteBuf content = null;
        if (length == 0) {
            content = new EmptyByteBuf(ctx.alloc());
        } else {
            content = ctx.alloc().buffer(length);
            in.readBytes(content);
        }
        packet.setId(id);
        packet.setLength(length);
        packet.setContent(content);
        out.add(packet);
    }
}
