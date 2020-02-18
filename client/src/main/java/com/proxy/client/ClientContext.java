package com.proxy.client;

import base.CryptoImpl;
import base.interfaces.Crypto;
import lombok.Data;

/**
 * @author kikyou
 * Created at 2020/2/18
 */
public class ClientContext {

    public static int id = -1;
    public static byte[] iv;
    public static Crypto crypto;

    public static void initContext(int id, byte[] iv) {
        ClientContext.id = id;
        ClientContext.iv = iv;
        ClientContext.crypto = new CryptoImpl(id, iv);
    }

}
