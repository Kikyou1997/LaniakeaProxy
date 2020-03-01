package base.arch;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * 为确保bytebuf的字节数足够凑成一个新的应用层报文，应该将此Decoder置于LengthFieldBasedDecoder之后
 */
public class DataTransmissionPacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte code = in.readByte();
        int id = in.readInt();
        int length = in.readInt();
        ByteBuf content = ctx.alloc().buffer(length);
        in.readBytes(content);
        LaniakeaPakcet packet = new LaniakeaPakcet(code, id, length, content);
        out.add(packet);
        ReferenceCountUtil.safeRelease(in);
    }
}
