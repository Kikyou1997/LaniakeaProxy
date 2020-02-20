package com.proxy.client;

import base.*;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * Created at 2020/2/16
 */
@Slf4j
public class HttpConnectionStream extends AbstractConnectionStream {


    public HttpConnectionStream(C_Client2ProxyConnection c2PConnection, ChannelHandlerContext context) {
        super(c2PConnection,context);
        initStream();
    }

    protected void initStream() {

    }


}

