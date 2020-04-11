package base.crypto;

import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CFBCrypto extends AbstractCrypto {

    public CFBCrypto(int keySize, int cfbIvLength) {
        super(Crypto.AES, Crypto.CFB_PADDING, keySize, cfbIvLength);
    }

    @Override
    public ByteBuf handle(Object msg, ChannelHandlerContext ctx) {
        return super.handle(msg, ctx);
    }

    @Override
    public byte[] encrypt(byte[] raw, byte[] iv, byte[] key) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(super.cipher);
            SecretKeySpec keySpec = new SecretKeySpec(key, super.alg);
            IvParameterSpec parameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
            return cipher.doFinal(raw);
        } catch (Exception e) {
            throw new CryptoException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public byte[] decrypt(byte[] encrypted, byte[] iv, byte[] key) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(super.cipher);
            SecretKeySpec keySpec = new SecretKeySpec(key, super.alg);
            IvParameterSpec parameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new CryptoException(e.getMessage(), e.getCause());
        }
    }

}
