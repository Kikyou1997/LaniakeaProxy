package com.proxy.client;

import base.arch.Config;
import base.arch.CryptoUtil;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author kikyou
 * Created at 2020/2/20
 */
public class ClientCryptoImpl implements Crypto {

    private int id = ClientContext.id;
    private byte[] iv = ClientContext.iv;
    private byte[] sk = Config.config.getSecretKeyBin();

    @Override
    public ByteBuf encrypt(ByteBuf raw) {
        try {
            return CryptoUtil.encrypt(raw, iv, sk);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ByteBuf decrypt(ByteBuf cypherText) {
        try {
            return CryptoUtil.decrypt(cypherText,  sk, iv);
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
