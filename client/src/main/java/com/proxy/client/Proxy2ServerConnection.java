package com.proxy.client;

import base.*;
import base.constants.Packets;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public class Proxy2ServerConnection extends AbstractConnection {

    private SocketAddressEntry socketAddress;
    private Channel channel;
    private AbstractConnectionStream connectionStream;

    public Proxy2ServerConnection(SocketAddressEntry entry, AbstractConnectionStream stream) throws Exceptions.ConnectionTimeoutException {
        this.socketAddress = entry;
        this.connectionStream = stream;
        if (!buildConnection2Remote(entry)) {
            throw new Exceptions.ConnectionTimeoutException(entry);
        }
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

    }


    @Override
    public ChannelFuture writeData(ByteBuf data) {
        return remoteServer.writeAndFlush(data);
    }

    protected boolean buildConnection2Remote(SocketAddressEntry socketAddress) {
        String host = socketAddress.getHost();
        short port = socketAddress.getPort();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(super.eventLoops);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIME_OUT);
        bootstrap.handler(new P2SChannelInitializer());
        ChannelFuture future = bootstrap.connect(host, port);
        future.syncUninterruptibly();
        this.channel = future.channel();
        return future.isSuccess();
    }

    @Override
    protected void disconnect() {
        super.remoteServer.close();
    }

    private static class P2SChannelInitializer extends ChannelInitializer<NioSocketChannel> {
        @Override
        protected void initChannel(NioSocketChannel ch) throws Exception {
            ch.pipeline()
                    .addLast(new HeadersPrepender.RequestHeadersPrepender(ClientContext.id))
                    //  返回的包不包括id字段
                    .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, Packets.HEADERS_DATA_RESP_LEN, 4));

        }
    }

}
