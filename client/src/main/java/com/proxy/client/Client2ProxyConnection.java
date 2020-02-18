package com.proxy.client;

import base.AbstractConnection;

import static base.AbstractConnectionStream.ConnectionStep;

import base.AbstractConnectionStream;
import base.SocketAddressEntry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kikyou
 * Created at 2020/1/29
 */
public class Client2ProxyConnection extends AbstractConnection {

    private Map<String/*客户端套接字*/, Proxy2ServerConnection/*到远程服务器的连接*/> addressPort2ConnectionMap = new ConcurrentHashMap<>();

    private Channel clientChannel;

    private AbstractConnectionStream connectionStream = null;

    private SocketAddressEntry socketAddress;

    private ConnectionStep currentStep;

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
    protected void disconnect() {
        for (String s : addressPort2ConnectionMap.keySet()) {
            addressPort2ConnectionMap.get(s).disconnect();
        }
    }

    @Override
    public ChannelFuture writeData(ByteBuf data) {
        return clientChannel.writeAndFlush(data);
    }
}
