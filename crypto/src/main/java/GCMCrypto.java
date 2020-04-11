import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class GCMCrypto extends AbstractCrypto {

    private final int keySize;
    private final int gcmIvLength;
    private final int gcmTagLength;


    public GCMCrypto(int keySize, int gcmIvLength, int gcmTagLength) {
        super(Crypto.AES, Crypto.GCM_NOPADDING);
        this.keySize = keySize;
        this.gcmIvLength = gcmIvLength;
        this.gcmTagLength = gcmTagLength;
    }

    @Override
    public byte[] encrypt(byte[] raw, byte[] iv, byte[] key) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(super.cipher);
            SecretKeySpec keySpec = new SecretKeySpec(key, super.alg);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(gcmTagLength * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
            return cipher.doFinal(raw);
        } catch (Exception e) {
            throw new CryptoException(e.getStackTrace().toString(),e.getCause());
        }
    }


    @Override
    public byte[] decrypt(byte[] encrypted, byte[] iv, byte[] key) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(super.cipher);
            SecretKeySpec keySpec = new SecretKeySpec(key, super.alg);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(gcmTagLength * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw  new CryptoException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public ByteBuf handle(Object msg, ChannelHandlerContext ctx) {
        return null;
    }

    public byte[] generateKey() {
        return super.generateKey(this.keySize);
    }
}
