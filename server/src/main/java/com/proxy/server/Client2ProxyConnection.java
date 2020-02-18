package com.proxy.server;

import base.AbstractConnection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author kikyou
 * Created at 2020/1/29
 */
public class Client2ProxyConnection extends AbstractConnection {


    private Channel clientChannel;


    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) {
        if (clientChannel == null) {
            clientChannel = ctx.channel();
        }
        if (super.connectionStream == null) {
            connectionStream = new HttpConnectionStream(this, ctx);
        }
        if (currentStep == null) {
            try {
                connectionStream.then(msg);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            currentStep = connectionStream.nextStep();
        } else {
            currentStep.handle(msg, ctx);
        }
    }


    @Override
    public ChannelFuture writeData(ByteBuf data) {
        return clientChannel.writeAndFlush(data);
    }
}
