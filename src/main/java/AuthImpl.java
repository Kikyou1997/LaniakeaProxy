import abstracts.AbstractHandler;
import constants.ResponseCode;
import interfaces.Auth;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kikyou
 * Created at 2020/1/31
 */
public class AuthImpl extends AbstractHandler<ByteBuf, Void> implements Auth {

    private static AtomicInteger ids = new AtomicInteger(Integer.MIN_VALUE);
    private String name = null;

    @Override
    public boolean isValid(Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        byte[] receivedHash = new byte[Auth.HASH_LENGTH];
        buf.readBytes(receivedHash);
        byte[] usernameBytes = new byte[buf.readableBytes()];
        buf.readBytes(usernameBytes);
        String username = new String(usernameBytes, StandardCharsets.UTF_8);
        this.name = username;
        Config.User user = Config.getUserInfo(username);
        if (user == null) {
            return false;
        }
        byte[] validHash = CryptoUtil.getSHA256Hash(user.getSecretKey(), Clock.getTimeInBytes());
        return Arrays.equals(receivedHash, validHash);
    }

    @Override
    public Void handle(Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            if (isValid(msg)) {
                int id = ids.getAndIncrement();
                idTimeMap.put(id, System.currentTimeMillis());
                idNameMap.put(id, name);
                byte[] iv = CryptoUtil.ivGenerator();
                ByteBuf resp = createAuthResponse(id, iv);
                sendResponse(resp);
                idIvMap.put(id, iv);
            } else {
                context.channel().close();
            }
        }
        return null;
    }

    private ByteBuf createAuthResponse(int id, byte[] iv) {
        return MessageGenerator.generateDirectBuf(ResponseCode.AUTH_RESP, Converter.convertInteger2ByteBigEnding(id), iv);
    }


}