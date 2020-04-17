package base.arch;

import base.constants.ResponseCode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author kikyou
 * Created at 2020/2/1
 */
public class MessageUtil {

    private static final PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
    private static final int CLOCK_RESP_SIZE = 9;
    private static final int RETRY_TIMES = 3;


    public static ByteBuf generateDirectBuf(byte code, byte[]... content) {
        int totalLength = content == null ? 1 : 1 + content.length;
        ByteBuf buf = allocator.directBuffer(totalLength);
        buf.writeByte(code);
        if (content != null) {
            for (byte[] b : content) {
                buf.writeBytes(b);
            }
        }
        return buf;
    }

    public static ByteBuf generateClockResponse() {
        ByteBuf buf = allocator.directBuffer(CLOCK_RESP_SIZE);
        buf.writeByte(ResponseCode.CLOCK_RESP);
        buf.writeLong(Clock.getTime());
        return buf;
    }

    public static ByteBuf generateAuthRequest(String username, String password) {
        return null;
    }

    public static boolean sendSyncMsg(ChannelHandlerContext ctx, ByteBuf buf){
        return sendSyncMsg(ctx.channel(), buf);
    }

    public static boolean sendSyncMsg(Channel ctx, ByteBuf buf){
        ChannelFuture future = null;
        int count = 0;
        do {
            count++;
            try {
                future = ctx.writeAndFlush(buf).sync();
            } catch (InterruptedException e) {
                return false;
            }
        } while (!future.isSuccess() && count < RETRY_TIMES);
        return future.isSuccess();
    }


}
