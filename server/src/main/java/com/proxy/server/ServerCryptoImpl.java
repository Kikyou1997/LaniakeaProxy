package com.proxy.server;

import base.arch.Config;
import base.arch.Session;
import base.crypto.CryptoUtil;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author kikyou
 * Created at 2020/2/20
 */
@Slf4j
public class ServerCryptoImpl implements Crypto {

    private byte[] secretKey = null;
    private Session session = null;

    public ServerCryptoImpl(Session session) {
        Objects.requireNonNull(session);
        this.session = session;
        try {
            secretKey = Config.getUserSecretKeyBin(session.getUsername());
        } catch (NullPointerException e) {
            throw new SessionExpiredException();
        }
    }

    @Override
    public ByteBuf encrypt(ByteBuf raw) {
        raw.readerIndex(0);
        try {
            var iv = session.getIv();
            return CryptoUtil.encrypt(raw, iv, secretKey);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SessionExpiredException();
        }
    }

    /**
     * @return 只返回正文
     */
    @Override
    public ByteBuf decrypt(ByteBuf cypherText) {
        try {
            var iv = session.getIv();
            return CryptoUtil.decrypt(cypherText, iv, secretKey);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SessionExpiredException();
        }
    }


}
