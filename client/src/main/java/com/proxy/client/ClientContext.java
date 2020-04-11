package com.proxy.client;

import base.arch.CryptoUtil;
import base.interfaces.Crypto;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * Created at 2020/2/18
 */
@Slf4j
public class ClientContext {

    public static int id = -1;
    public static byte[] iv = CryptoUtil.generateIv();
    public static Crypto crypto;

    public static void initContext(int id) {
        ClientContext.id = id;
        ClientContext.crypto = new ClientCryptoImpl();
    }

    public static void failedThenQuit(ChannelFuture future) {
        future.syncUninterruptibly();
        if (!future.isSuccess()){
            log.error("Send failed", future.cause());
            System.exit(-1);
        }
    }

}
