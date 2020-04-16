package base.arch;

import io.netty.buffer.ByteBuf;
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
        byte code = in.readByte();
        LaniakeaPacket packet = new LaniakeaPacket();
        packet.setCode(code);
        switch (code) {
            case ResponseCode.CONN_EXPIRED:
                in.readInt();
                break;
            default:
                int id = in.readInt();
                int length = in.readInt();
                ByteBuf content = ctx.alloc().buffer(length);
                in.readBytes(content);
                packet.setId(id);
                packet.setLength(length);
                packet.setContent(content);
        }
        out.add(packet);
    }
}
