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
public abstract class HeadersPrepender extends MessageToMessageEncoder<ByteBuf> {


    protected int id;

    public HeadersPrepender(int id) {
        this.id = id;
    }

    public static class RequestHeadersPrepender extends HeadersPrepender {


        public RequestHeadersPrepender(int id) {
            super(id);
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
            int headerLength = Packets.HEADERS_DATA_REQ_LEN;
            int length = msg.readableBytes() +headerLength;
            out.add(ctx.alloc().buffer(Packets.FILED_CODE_LENGTH).writeByte(RequestCode.DATA_TRANS_REQ));
            out.add(ctx.alloc().buffer(Packets.FILED_LENGTH_LEN).writeInt(length));
            out.add(ctx.alloc().buffer(Packets.FILED_ID_LENGTH).writeInt(id));

        }
    }

    public static class ResponseHeadersPrepender extends HeadersPrepender {

        public ResponseHeadersPrepender(int id) {
            super(id);
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
            int length = msg.readableBytes() + Packets.HEADERS_DATA_RESP_LEN;
            out.add(ctx.alloc().buffer(Packets.FILED_CODE_LENGTH).writeByte(ResponseCode.DATA_TRANS_RESP));
            out.add(ctx.alloc().buffer(Packets.FILED_LENGTH_LEN).writeInt(length));
        }
    }


}
