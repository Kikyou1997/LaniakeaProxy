import constants.ResponseCode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * @author kikyou
 * Created at 2020/2/1
 */
public class MessageGenerator {

    private static final PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
    private static final int CLOCK_RESP_SIZE = 9;


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

}
