/**
 * @author kikyou
 * Created at 2020/1/31
 */
public class Exceptions {

    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
    }

}
