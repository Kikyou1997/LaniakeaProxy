package com.proxy.client;

import base.*;
import base.constants.Packets;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * @date 2020/1/29
 */
@Slf4j
public class Proxy2ServerConnection extends AbstractConnection {

    private SocketAddressEntry socketAddress;

    public Proxy2ServerConnection(SocketAddressEntry entry, AbstractConnectionStream stream) throws Exceptions.ConnectionTimeoutException {
        this.socketAddress = entry;
        this.connectionStream = stream;
        if (!buildConnection2Remote(entry)) {
            throw new Exceptions.ConnectionTimeoutException(entry);
        }
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if (currentStep == null) {
            currentStep = connectionStream.nextStep();
        }
        currentStep.handle(msg, ctx);
    }


    @Override
    public ChannelFuture writeData(ByteBuf data) {
        return remoteServer.writeAndFlush(data);
    }

    protected boolean buildConnection2Remote(SocketAddressEntry socketAddress) {
        String host = socketAddress.getHost();
        short port = socketAddress.getPort();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(new NioEventLoopGroup(1));
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new P2SChannelInitializer());
        ChannelFuture future = bootstrap.connect(host, port).syncUninterruptibly();
        this.remoteServer = future.channel();
        if (!future.isSuccess()) {
            log.error("Connect 2 remote proxy server " + socketAddress.toString() + " failed", future.cause());
            return false;
        }
        return true;
    }

    @Override
    public void disconnect() {
        super.remoteServer.close();
    }

    private static class P2SChannelInitializer extends ChannelInitializer<NioSocketChannel> {
        @Override
        protected void initChannel(NioSocketChannel ch) throws Exception {
            ch.pipeline()
                    .addLast(new HeadersPrepender.RequestHeadersPrepender(ClientContext.id))
                    //  返回的包不包括id字段
                    .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, Packets.FIELD_CODE_LENGTH, 4));

        }
    }

}
