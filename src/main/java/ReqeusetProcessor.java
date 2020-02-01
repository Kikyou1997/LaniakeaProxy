import static constants.RequestCode.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * @author kikyou
 * Created at 2020/2/1
 */
public class ReqeusetProcessor extends SimpleChannelInboundHandler<ByteBuf> {

    private Client2ProxyConnection client2ProxyConnection = null;

    public ReqeusetProcessor(Client2ProxyConnection client2ProxyConnection) {
        this.client2ProxyConnection = client2ProxyConnection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte requestCode = getRequestCode(msg);
        switch (requestCode) {
            case GET_CLOCK_REQ:
                ByteBuf buf = MessageGenerator.generateClockResponse();
                ctx.channel().writeAndFlush(buf);
                ReferenceCountUtil.release(buf);
                break;
            case DATA_TRANS_REQ:

            default:
                break;

        }
    }

    private byte getRequestCode(ByteBuf msg) {
        return msg.readByte();
    }
}
