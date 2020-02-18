package base;

import base.constants.Packets;
import base.constants.RequestCode;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 无状态
 *
 * @author kikyou
 * Created at 2020/2/1
 */
public class CryptoImpl extends AbstractHandler<ByteBuf> implements Crypto {

    private static final int ID_POS = Packets.FIELD_CODE_LENGTH;

    private byte[] getIv(int id) {
        return idIvMap.get(id);
    }

    public CryptoImpl() {
    }

    public CryptoImpl(int clientId, byte[] iv) {
        super(clientId, iv);
    }

    @Override
    public ByteBuf encrypt(ByteBuf raw) throws RuntimeException {
        String name = super.getUsernameById(raw);
        byte[] secrets = Config.getUserSecretKeyBin(name);
        byte[] iv = getIv(getId(raw));
        byte[] encrypted = new byte[raw.readableBytes()];
        raw.readBytes(encrypted);
        try {
            encrypted = CryptoUtil.encrypt(encrypted, secrets, iv);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        raw.clear();
        raw.writeBytes(encrypted);
        return raw;
    }

    /*
    *返回解密完的自定义头的原数据
    */
    public ByteBuf decrypt(ByteBuf cypherText, int textPosition) {
        String name = super.getUsernameById(cypherText);
        byte[] secrets = Config.getUserSecretKeyBin(name);
        byte[] iv = getIv(getId(cypherText));
        cypherText.readerIndex(textPosition);
        byte[] raw = new byte[cypherText.readableBytes()];
        cypherText.readBytes(raw);
        try {
            raw = CryptoUtil.decrypt(raw, secrets, iv);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        cypherText.clear();
        cypherText.writeBytes(raw);
        return cypherText;
    }

    @Override
    public ByteBuf decrypt(ByteBuf cypherText) throws RuntimeException {
        cypherText.readerIndex(0);
        byte code = cypherText.readByte();
        decrypt(cypherText, code == RequestCode.CONNECT ? Packets.HEADERS_CONNECT_REQ_LEN : Packets.HEADERS_DATA_REQ_LEN);
        return cypherText;
    }

    @Override
    public ByteBuf handle(Object msg, ChannelHandlerContext ctx) throws RuntimeException {
        super.context = ctx;
        ByteBuf buf = (ByteBuf) msg;
        return decrypt(buf);
    }

}
