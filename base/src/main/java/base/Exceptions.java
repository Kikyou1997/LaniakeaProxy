package base;

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

    public static class ConnectionTimeoutException extends Exception {
        public ConnectionTimeoutException(SocketAddressEntry entry) {
            super("Connect to " + entry.toString() + " timeout");
        }
    }

}
