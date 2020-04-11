package base.crypto;

public class CryptoException extends RuntimeException {

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
