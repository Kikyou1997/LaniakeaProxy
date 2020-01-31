package interfaces;

import io.netty.buffer.ByteBuf;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public interface Crypto extends Handler {

    byte[] encrypt(ByteBuf raw);

    byte[] decrypt(ByteBuf cypherText);
}
