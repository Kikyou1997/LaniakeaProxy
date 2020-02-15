package base.interfaces;

import io.netty.buffer.ByteBuf;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public interface Crypto extends Handler<ByteBuf> {

    /*
    * 一字节请求码 4字节长度字段
    */
    int CRYPTO_INDEX = 5;

    ByteBuf encrypt(ByteBuf raw) throws Exception;

    ByteBuf decrypt(ByteBuf cypherText) throws Exception;
}
