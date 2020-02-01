import interfaces.Auth;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author kikyou
 * Created at 2020/1/31
 */
public class AuthImpl implements Auth {


    @Override
    public boolean isValid(Object msg) {
        ByteBuf buf = (ByteBuf)msg;
        byte[] receivedHash = new byte[Auth.HASH_LENGTH];
        buf.readBytes(receivedHash);
        byte[] usernameBytes = new byte[buf.readableBytes()];
        buf.readBytes(usernameBytes);
        String username = new String(usernameBytes, StandardCharsets.UTF_8);
        Config.User user = Config.getUserInfo(username);
        if (user == null) {
            return false;
        }
        byte[] validHash = CryptoUtil.getSHA256Hash(user.getSecretKey(), Clock.getTimeInBytes());
        return Arrays.equals(receivedHash, validHash);
    }

    @Override
    public void handle(Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            isValid(msg);
        } else {
            throw new Exceptions.AuthenticationException("Invalid message type");
        }
    }


}
