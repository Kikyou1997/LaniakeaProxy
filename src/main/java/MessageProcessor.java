import static constants.RequestCode.*;

import constants.Packets;
import constants.RequestCode;
import interfaces.Handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 只用于代理服务端 对来自客户端的请求的处理
 *
 * @author kikyou
 * Created at 2020/2/1
 */
public class MessageProcessor extends SimpleChannelInboundHandler<ByteBuf> {

    private Map<Byte, Handler> byteHandlerMap = new ConcurrentHashMap<>();
    // 2 + 1 + 4 + 4 = 11
    private static final int HEADER_LENGTH = Packets.MAGIC_LENGTH + Packets.CODE_LENGTH + Packets.ID_LENGTH + Packets.LENGTH_FILED_LENGTH;
    // 对于CONNECT请求
    private static final int DEST_POS = Packets.MAGIC_LENGTH + Packets.CODE_LENGTH;

    {
        byteHandlerMap.put(AUTH_REQ, new AuthImpl());
        byteHandlerMap.put(DATA_TRANS_REQ, new CryptoImpl());
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte requestCode = getRequestCode(msg);
        Handler handler = null;
        switch (requestCode) {
            case GET_CLOCK_REQ:
                ByteBuf buf = MessageGenerator.generateClockResponse();
                ctx.channel().writeAndFlush(buf);
                ReferenceCountUtil.release(buf);
                return;
            case AUTH_REQ:
                handler = byteHandlerMap.get(AUTH_REQ);
                handler.setContext(ctx);
                handler.handle(msg);
                return;
            case DATA_TRANS_REQ:
                handler = byteHandlerMap.get(DATA_TRANS_REQ);
                handler.setContext(ctx);
                handler.handle(msg);
                break;
            case CONNECT:
                byte ipv = getIpAddressVersion(msg);
                byte[] address = null;
                int port = 0;
                if (ipv == Packets.IPV4) {
                    address = new byte[Packets.IPV4_LENGTH];
                    msg.readBytes(address);
                    port = (int) msg.readShort();
                } else if (ipv == Packets.IPV6) {
                    address = new byte[Packets.IPV6_LENGTH];
                    msg.readBytes(address);
                    port = (int)msg.readShort();
                }

        }
        removeRedundantHeader(msg);
        ctx.fireChannelRead(msg);
    }

    public static byte getRequestCode(ByteBuf msg) {
        msg.readerIndex(Packets.MAGIC_LENGTH);
        return msg.readByte();
    }

    private void removeRedundantHeader(ByteBuf buf) {
        buf.readerIndex(HEADER_LENGTH);
        byte[] msg = new byte[buf.readableBytes()];
        buf.readBytes(msg);
        buf.clear();
        buf.writeBytes(msg);
    }

    private byte getIpAddressVersion(ByteBuf buf) {
        buf.readerIndex(DEST_POS);
        return buf.readByte();
    }
}
