package com.proxy.server;

import base.AbstractHandler;
import base.Config;
import base.CryptoUtil;
import base.constants.RequestCode;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * Created at 2020/2/20
 */
@Slf4j
public class ServerCryptoImpl implements Crypto {

    private int id;
    private byte[] secretKey = null;
    private byte[] iv = null;

    public ServerCryptoImpl(int id) {
        this.id = id;
        secretKey = Config.getUserSecretKeyBin(ServerContext.idNameMap.get(id));
        iv = ServerContext.idIvMap.get(id);
    }

    public ServerCryptoImpl() {
    }

    @Override
    public ByteBuf encrypt(ByteBuf raw) {
        raw.readerIndex(0);
        byte[] iv = ServerContext.idIvMap.get(id);
        byte[] key = Config.getUserSecretKeyBin(ServerContext.idNameMap.get(id));
        try {
            return CryptoUtil.encrypt(raw, iv, key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return 只返回正文
     */
    @Override
    public ByteBuf decrypt(ByteBuf cypherText) {
        try {
            return CryptoUtil.decrypt(cypherText, secretKey, iv);
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