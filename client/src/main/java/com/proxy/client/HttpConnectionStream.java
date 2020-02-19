package com.proxy.client;

import base.*;
import base.constants.RequestCode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author kikyou
 * Created at 2020/2/16
 */
@Slf4j
public class HttpConnectionStream extends AbstractConnectionStream {


    public HttpConnectionStream(Client2ProxyConnection c2PConnection, ChannelHandlerContext context) {
        super(c2PConnection,context);
        initStream();
    }

    protected void initStream() {

    }


}

