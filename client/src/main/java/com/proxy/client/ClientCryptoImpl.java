package com.proxy.client;

import base.arch.Config;
import base.arch.HexDump;
import base.crypto.CryptoUtil;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * Created at 2020/2/20
 */
@Slf4j
public class ClientCryptoImpl implements Crypto {

    private final byte[] sk = Config.config.getSecretKeyBin();

    @Override
    public ByteBuf encrypt(ByteBuf raw) {
        byte[] iv = ClientContext.getIv();
        try {
            return CryptoUtil.encrypt(raw, iv, sk);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ByteBuf decrypt(ByteBuf cypherText) {
        byte[] iv = ClientContext.getIv();
        try {
            log.debug("iv " + HexDump.dump(null, iv));
            log.debug("sk " + HexDump.dump(null, sk));
            return CryptoUtil.decrypt(cypherText, iv, sk);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ByteBuf handle(Object msg, ChannelHandlerContext ctx) {
        return null;
    }
}
