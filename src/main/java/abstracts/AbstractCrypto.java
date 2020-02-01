package abstracts;

import interfaces.Crypto;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author kikyou
 * Created at 2020/1/31
 */
public abstract class AbstractCrypto implements Crypto {

    private static MessageDigest digest = null;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getSHA256Hash(String password, byte[] salt) {
        digest.update(password.getBytes(StandardCharsets.UTF_8));
        return digest.digest(salt);
    }

    public abstract byte[] encrypt(ByteBuf raw);

    public abstract byte[] decrypt(ByteBuf cypherText);

    public abstract void handle(Object msg) throws Exception;

}
