package base.interfaces;

import io.netty.buffer.ByteBuf;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public interface Crypto extends Handler<ByteBuf> {


    ByteBuf encrypt(ByteBuf raw) throws Exception;

    ByteBuf decrypt(ByteBuf cypherText) throws Exception;
}
