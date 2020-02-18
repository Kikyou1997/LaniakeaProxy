package com.proxy.client;

import base.AbstractConnection;

import base.SocketAddressEntry;
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

    private SocketAddressEntry socketAddress;


    // 该构造器适用于 服务端模式
    public Client2ProxyConnection(Channel channel) {
        this.clientChannel = channel;
    }


    public Client2ProxyConnection() {
    }


    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if (connectionStream == null) {
            connectionStream = new HttpConnectionStream(this, ctx);
            connectionStream
                    .then(msg)
                    .then(msg);
        } else {
            if (currentStep == null) {
                currentStep = connectionStream.currentStep();
            }
            currentStep.handle(msg, ctx);
        }
    }


    @Override
    public ChannelFuture writeData(ByteBuf data) {
        return clientChannel.writeAndFlush(data);
    }



}
