package base.crypto;

import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;

public abstract class AbstractCrypto implements Crypto {

    protected final String alg;
    protected final String cipher;
    protected final int keySize;
    protected final int ivLength;

    public AbstractCrypto(String alg, String cipher, int keySize, int ivLength) {
        this.alg = alg;
        this.cipher = cipher;
        this.keySize = keySize;
        this.ivLength = ivLength;
    }

    @Override
    public ByteBuf encrypt(ByteBuf raw) {
        return null;
    }

    @Override
    public ByteBuf decrypt(ByteBuf cypherText) {
        return null;
    }

    @Override
    public ByteBuf handle(Object msg, ChannelHandlerContext ctx) {
        return null;
    }

    public abstract byte[] encrypt(byte[] raw, byte[] iv, byte[] key);

    public abstract byte[] decrypt(byte[] encrypted, byte[] iv, byte[] key);

    public byte[] generateKey(int length) {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(this.alg);
            kg.init(length);
            SecretKey secretKey = kg.generateKey();
            return secretKey.getEncoded();
        } catch (Exception e) {
            throw new CryptoException(e.getMessage(), e.getCause());
        }
    }

    public byte[] generateIv(int length) {
        SecureRandom r = new SecureRandom();
        byte[] iv = new byte[length];
        r.nextBytes(iv);
        return iv;
    }

    public byte[] generateIv() {
        return generateIv(this.ivLength);
    }

    public byte[] generateKey() {
        return generateKey(this.keySize);
    }



}
