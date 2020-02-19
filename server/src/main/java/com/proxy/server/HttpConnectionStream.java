package com.proxy.server;

import base.*;
import base.constants.Packets;
import base.constants.RequestCode;
import base.constants.ResponseCode;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

/**
 * @author kikyou
 * Created at 2020/2/18
 */
public class HttpConnectionStream extends AbstractConnectionStream {




    public HttpConnectionStream(AbstractConnection c2PConnection, ChannelHandlerContext context) {
        super(c2PConnection, context);
    }

    @Override
    protected void initStream() {
    }
}
