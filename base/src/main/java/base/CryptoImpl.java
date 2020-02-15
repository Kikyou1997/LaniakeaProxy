package base;

import base.constants.Packets;
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

    private static final int ID_POS = REQ_CODE_POS + Packets.CODE_LENGTH;
    private static final int TEXT_POS = ID_POS + Packets.ID_LENGTH;

    private byte[] getIv(int id) {
        return idIvMap.get(id);
    }

    public CryptoImpl() {
    }

    public CryptoImpl(int clientId, byte[] iv) {
        super(clientId, iv);
    }

    @Override
    protected int getId(ByteBuf buf) {
        buf.readerIndex(ID_POS);
        return buf.readInt();
    }

    @Override
    public ByteBuf encrypt(ByteBuf raw) throws Exception {
        String name = super.getUsernameById(raw);
        byte[] secrets = Config.getUserSecretKeyBin(name);
        byte[] iv = getIv(getId(raw));
        raw.readerIndex(Crypto.CRYPTO_INDEX);
        byte[] encrypted = new byte[raw.readableBytes()];
        raw.readBytes(encrypted);
        encrypted = CryptoUtil.encrypt(encrypted, secrets, iv);
        raw.clear();
        raw.writeBytes(encrypted);
        return raw;
    }

    @Override
    public ByteBuf decrypt(ByteBuf cypherText) throws Exception {
        String name = super.getUsernameById(cypherText);
        byte[] secrets = Config.getUserSecretKeyBin(name);
        byte[] iv = getIv(getId(cypherText));
        cypherText.readerIndex(TEXT_POS);
        byte[] raw = new byte[cypherText.readableBytes()];
        cypherText.readBytes(raw);
        raw = CryptoUtil.decrypt(raw, secrets, iv);
        cypherText.clear();
        cypherText.writeBytes(raw);
        return cypherText;
    }


    @Override
    public ByteBuf handle(Object msg, ChannelHandlerContext ctx) throws Exception {
        super.context = ctx;
        ByteBuf buf = (ByteBuf) msg;
        if (isProxyMessage(buf)) {
            if (AbstractProxy.CLIENT_MODE) {
                return decrypt(buf);
            } else {
                return encrypt(buf);
            }
        } else {
            if (AbstractProxy.CLIENT_MODE) {
                return encrypt(buf);
            } else {
                return decrypt(buf);
            }
        }
    }
}
