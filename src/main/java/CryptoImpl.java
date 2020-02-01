import abstracts.AbstractHandler;
import interfaces.Crypto;
import io.netty.buffer.ByteBuf;

import static constants.RequestCode.DATA_TRANS_REQ;
import static constants.ResponseCode.DATA_TRANS_RESP;

/**
 * @author kikyou
 * Created at 2020/2/1
 */
public class CryptoImpl extends AbstractHandler<ByteBuf, ByteBuf> implements Crypto {

    @Override
    public ByteBuf encrypt(ByteBuf raw) {
        raw.readerIndex(Crypto.CRYPTO_INDEX);

    }

    @Override
    public ByteBuf decrypt(ByteBuf cypherText) {
    }

    @Override
    public ByteBuf handle(Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte code = ReqeusetProcessor.getRequestCode(buf);
        switch (code) {
            case DATA_TRANS_REQ:
                return encrypt(buf);
                // 奇妙深刻
                break;
            case DATA_TRANS_RESP:
                return decrypt(buf);
                break;
        }
    }
}
