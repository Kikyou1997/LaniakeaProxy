package base.crypto;

import base.arch.ProxyUtil;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author kikyou
 */
@Slf4j
public class CryptoUtil {

    private static Base64.Encoder base64Encoder = Base64.getEncoder();
    private static Base64.Decoder base64Decoder = Base64.getDecoder();
    private static AbstractCrypto crypto = new CFBCrypto(256, 16);

    public static byte[] getSHA256Hash(byte[] secretKey, byte[] salt) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        digest.update(secretKey);
        return digest.digest(salt);
    }

    public static byte[] getSHA256Hash(String secretKey, byte[] salt) {
        return getSHA256Hash(base64Decoder.decode(secretKey), salt);
    }


    public static byte[] decrypt(byte[] data, byte[] iv, byte[] key) {
        return crypto.decrypt(data, iv, key);
    }

    public static byte[] encrypt(byte[] data, byte[] iv, byte[] key) {
        return crypto.encrypt(data, iv, key);
    }

    public static byte[] generateKey() {
        return crypto.generateKey();
    }

    public static byte[] generateIv() {
        return crypto.generateIv();
    }

    /**
     * @return 返回一个新的加密过的ByteBuf实例
     */
    public static ByteBuf encrypt(ByteBuf data, byte[] iv, byte[] secretKey) throws Exception {
        byte[] re = encrypt(ProxyUtil.getBytesFromByteBuf(data), iv, secretKey);
        return ProxyUtil.getByteBufFromBytes(re);
    }


    public static ByteBuf decrypt(ByteBuf data, byte[] iv, byte[] secretKey) throws Exception {
        byte[] re = decrypt(ProxyUtil.getBytesFromByteBuf(data), iv, secretKey);
        return ProxyUtil.getByteBufFromBytes(re);
    }

    public static byte[] base64Decode(String encoded) {
        return base64Decoder.decode(encoded);
    }

    public static String base64Encode(byte[] bytes) {
        return base64Encoder.encodeToString(bytes);
    }

    public static void setCrypto(AbstractCrypto crypto) {
        CryptoUtil.crypto = crypto;
    }
}
