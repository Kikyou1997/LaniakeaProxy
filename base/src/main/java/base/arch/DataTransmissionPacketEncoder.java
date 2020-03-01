package base.arch;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class DataTransmissionPacketEncoder extends MessageToByteEncoder<LaniakeaPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, LaniakeaPacket msg, ByteBuf out) throws Exception {
        out.writeByte(msg.getCode());
        out.writeInt(msg.getId());
        out.writeInt(msg.getLength());
        out.writeBytes(msg.getContent());
    }
}
