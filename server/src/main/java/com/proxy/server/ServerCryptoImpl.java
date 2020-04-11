package com.proxy.server;

import base.arch.Config;
import base.arch.HexDump;
import base.crypto.CryptoException;
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
public class ServerCryptoImpl implements Crypto {

    private int id;
    private byte[] secretKey = null;
    private byte[] iv = null;

    public ServerCryptoImpl(int id) {
        this.id = id;
        secretKey = Config.getUserSecretKeyBin(ServerContext.getSession(id).getUsername());
        iv = ServerContext.getSession(id).getIv();
    }

    public ServerCryptoImpl() {
    }

    @Override
    public ByteBuf encrypt(ByteBuf raw) {
        raw.readerIndex(0);
        try {
            return CryptoUtil.encrypt(raw, iv, secretKey);
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
            return CryptoUtil.decrypt(cypherText, iv, secretKey);
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
