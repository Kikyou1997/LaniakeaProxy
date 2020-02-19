package com.proxy.client;

import base.CryptoImpl;
import base.interfaces.Crypto;
import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * Created at 2020/2/18
 */
@Slf4j
public class ClientContext {

    public static int id = -1;
    public static byte[] iv;
    public static Crypto crypto;

    public static void initContext(int id, byte[] iv) {
        ClientContext.id = id;
        ClientContext.iv = iv;
        ClientContext.crypto = new CryptoImpl(id, iv);
    }

    public static void failedThenQuit(ChannelFuture future) {
        future.syncUninterruptibly();
        if (!future.isSuccess()){
            log.error("Send failed", future.cause());
            System.exit(-1);
        }
    }

}
