package base.interfaces;

import io.netty.buffer.ByteBuf;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public interface Crypto {

    String AES = "AES";
    String CFB_PADDING = "AES/CFB/PKCS5Padding";
    String GCM_NOPADDING = "AES/GCM/NoPadding";

    String CFB = "aes-192-cfb";
    String GCM = "aes-256-gcm";

    ByteBuf encrypt(ByteBuf raw);

    ByteBuf decrypt(ByteBuf cypherText);
}
