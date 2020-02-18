package base;

import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;

/**
 * @author kikyou
 * Created at 2020/1/31
 */
public abstract class AbstractCrypto implements Crypto {



    public abstract ByteBuf encrypt(ByteBuf raw);

    public abstract ByteBuf decrypt(ByteBuf cypherText);

    public abstract void handle(Object msg) throws Exception;

}
