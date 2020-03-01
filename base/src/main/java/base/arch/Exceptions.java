package base.arch;


import io.netty.channel.Channel;

/**
 * @author kikyou
 * Created at 2020/1/31
 */
public class Exceptions {

    public static class AuthenticationFailedException extends RuntimeException {
        public AuthenticationFailedException(String message) {
            super(message);
        }
    }

    public static class ConnectionTimeoutException extends RuntimeException {
        public ConnectionTimeoutException(SocketAddressEntry entry) {
            super("Connect to " + entry.toString() + " timeout");
        }
    }

    public static class ChannelUnwritable extends RuntimeException {
        public ChannelUnwritable(Channel c) {
            super(ProxyUtil.getRemoteAddressAndPortFromChannel(c) + "is not writable");
        }
    }

}
