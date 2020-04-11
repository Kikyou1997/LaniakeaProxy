package base.interfaces;

import io.netty.buffer.ByteBuf;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public interface Crypto extends Handler<ByteBuf> {

    String AES = "AES";
    String CFB_PADDING = "AES/CFB/PKCS5Padding";
    String GCM_NOPADDING = "AES/GCM/NoPadding";


    ByteBuf encrypt(ByteBuf raw);

    ByteBuf decrypt(ByteBuf cypherText);
}
