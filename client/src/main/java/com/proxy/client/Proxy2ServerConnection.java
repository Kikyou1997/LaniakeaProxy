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


    public Proxy2ServerConnection(SocketAddressEntry entry, AbstractConnection c2PConnection) throws Exceptions.ConnectionTimeoutException {
        super.c2PConnection = c2PConnection;
        if (!buildConnection2Remote(entry)) {
            throw new Exceptions.ConnectionTimeoutException(entry);
        }
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        ByteBuf decrypted = ClientContext.crypto.decrypt(msg);
        c2PConnection.writeData(decrypted).syncUninterruptibly().isSuccess();
    }


    @Override
    public ChannelFuture writeData(ByteBuf data) {
        return channel.writeAndFlush(data);
    }

    protected boolean buildConnection2Remote(SocketAddressEntry socketAddress) {
        String host = socketAddress.getHost();
        short port = socketAddress.getPort();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(new NioEventLoopGroup(1));
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.handler(new P2SChannelInitializer());
        ChannelFuture future = bootstrap.connect(host, port).syncUninterruptibly();
        this.channel = future.channel();
        if (!future.isSuccess()) {
            log.error("Connect 2 remote proxy server " + socketAddress.toString() + " failed", future.cause());
            return false;
        }
        return true;
    }

    @Override
    public void disconnect() {
         channel.close();
    }

    private static class P2SChannelInitializer extends ChannelInitializer<NioSocketChannel> {
        @Override
        protected void initChannel(NioSocketChannel ch) throws Exception {
            ch.pipeline()
                    //  返回的包不包括id字段
                    .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, Packets.FIELD_CODE_LENGTH, 4));

        }
    }

}
