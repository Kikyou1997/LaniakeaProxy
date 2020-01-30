import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;

/**
 * @author kikyou
 */
@Slf4j
public class CryptoUtil {

    private static final String ALG = "AES";
    private static final String CIPHER = "AES/CFB/PKCS5Padding";

    public static Key toKey(byte[] key) {
        return new SecretKeySpec(key, ALG);
    }

    public static Cipher initCipher(Key key, IvParameterSpec ivParameterSpec) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
            return cipher;
        } catch (Exception e) {
            throw new Error("Cipher init failed");
        }
    }

    public static byte[] decrypt(byte[] data, byte[] key, IvParameterSpec ivParameterSpec) throws Exception {
        Key k = toKey(key);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, k, ivParameterSpec);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        IvParameterSpec spec = new IvParameterSpec(iv);
        return decrypt(data, key, spec);
    }

    public static byte[] encrypt(byte[] data, byte[] key, IvParameterSpec ivParameterSpec) throws Exception {
        Key k = toKey(key);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, k, ivParameterSpec);
        return cipher.doFinal(data);
    }

    public static byte[] encrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        IvParameterSpec spec = new IvParameterSpec(iv);
        return encrypt(data, key, spec);
    }

    public static byte[] initKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALG);
            keyGenerator.init(192);
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey.getEncoded();
        } catch (Exception e) {
            log.error("Error occurred when generate key", e);
            throw new Error();
        }
    }

    public static ByteBuf encryptByteBuf(Object byteBuf, byte[] key, IvParameterSpec ivParameterSpec) throws Exception {
        if (byteBuf instanceof ByteBuf) {
            return ProxyUtil.getByteBufFromBytes(encrypt(ProxyUtil.getBytesFromByteBuf((ByteBuf) byteBuf), key, ivParameterSpec));
        }
        throw new IllegalArgumentException("ByteBuf required");
    }

    public static ByteBuf decryptByteBuf(Object byteBuf, byte[] key, IvParameterSpec ivParameterSpec) throws Exception {
        if (byteBuf instanceof ByteBuf) {
            return ProxyUtil.getByteBufFromBytes(decrypt(ProxyUtil.getBytesFromByteBuf((ByteBuf) byteBuf), key, ivParameterSpec));
        }
        throw new IllegalArgumentException("ByteBuf required");
    }

    public static ByteBuf decryptByteBuf(Object data, byte[] key, byte[] iv) throws Exception {
        IvParameterSpec spec = new IvParameterSpec(iv);
        return decryptByteBuf(data, key, spec);
    }

    public static IvParameterSpec ivGenerator() {
        SecureRandom random = new SecureRandom();
        return new IvParameterSpec(random.generateSeed(16));
    }

    public static Object encrypt(ByteBuf data, byte[] id, byte[] iv, byte[] secretKey) throws Exception {
        byte[] re = encrypt(ProxyUtil.getBytesFromByteBuf(data), secretKey, iv);
        return ProxyUtil.getByteBufFromBytes(re);
    }

    public static Object encrypt(Object data, byte[] id) throws Exception {
        return encrypt(data, id);
    }

    public static Object decrypt(ByteBuf data, byte[] secretKey, byte[] iv) throws Exception {
        byte[] re = decrypt(ProxyUtil.getBytesFromByteBuf(data), secretKey, iv);
        return ProxyUtil.getByteBufFromBytes(re);
    }

    public static Object decrypt(Object data, byte[] id) throws Exception {
        return decrypt(data, id);
    }


}