package base;

import base.constants.Packets;
import base.constants.RequestCode;
import base.constants.ResponseCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author kikyou
 * Created at 2020/2/14
 */
public class HeadersPrepender extends MessageToMessageEncoder<ByteBuf> {

    private static int BASIC_HEADER_LENGTH = Packets.MAGIC_LENGTH + Packets.CODE_LENGTH + Packets.LENGTH_FILED_LENGTH;

    protected int id;

    public HeadersPrepender(int id) {
        this.id = id;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        out.add(
                ctx.alloc()
                        .buffer(Packets.MAGIC_LENGTH)
                        .writeShort(Packets.MAGIC));
    }

    public static class RequestHeadersPrepender extends HeadersPrepender {

        public RequestHeadersPrepender(int id) {
            super(id);
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
            int length = msg.readableBytes() + BASIC_HEADER_LENGTH + Packets.ID_LENGTH;
            super.encode(ctx, msg, out);
            out.add(ctx.alloc().buffer(Packets.CODE_LENGTH).writeByte(RequestCode.DATA_TRANS_REQ));
            out.add(ctx.alloc().buffer(Packets.LENGTH_FILED_LENGTH).writeInt(length));
            out.add(ctx.alloc().buffer(Packets.ID_LENGTH).writeInt(id));

        }
    }

    public static class ResponseHeadersPrepender extends HeadersPrepender {

        public ResponseHeadersPrepender(int id) {
            super(id);
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
            int length = msg.readableBytes() + BASIC_HEADER_LENGTH;
            super.encode(ctx, msg, out);
            out.add(ctx.alloc().buffer(Packets.CODE_LENGTH).writeByte(ResponseCode.DATA_TRANS_RESP));
            out.add(ctx.alloc().buffer(Packets.LENGTH_FILED_LENGTH).writeInt(length));
        }
    }


}
