import interfaces.Auth;
import io.netty.buffer.ByteBuf;

/**
 * @author kikyou
 * Created at 2020/1/31
 */
public class AuthImpl implements Auth {

    @Override
    public boolean isValid(Object msg) {
        ByteBuf buf = (ByteBuf)msg;

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
