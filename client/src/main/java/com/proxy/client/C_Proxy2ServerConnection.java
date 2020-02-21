package com.proxy.client;

import base.*;
import base.constants.Packets;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
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
public class C_Proxy2ServerConnection extends AbstractConnection {

    //private static int temp = 0;

    public C_Proxy2ServerConnection(SocketAddressEntry entry, AbstractConnection c2PConnection) throws Exceptions.ConnectionTimeoutException {
        super.c2PConnection = c2PConnection;
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(msg.readableBytes() - Packets.HEADERS_DATA_RESP_LEN);
        msg.readerIndex(Packets.HEADERS_DATA_RESP_LEN);
        msg.readBytes(buf);
        buf.readerIndex(0);
        ByteBuf decrypted = ClientContext.crypto.decrypt(buf);
        c2PConnection.writeData(decrypted).syncUninterruptibly().isSuccess();
    }


    @Override
    public ChannelFuture writeData(ByteBuf data) {
        return channel.writeAndFlush(data);
    }

    @Override
    public ChannelFuture buildConnection2Remote(SocketAddressEntry socketAddress) {
        String host = socketAddress.getHost().trim();
        short port = socketAddress.getPort();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(new NioEventLoopGroup(1));
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                try {

                    ch.pipeline().addLast(new TempDecoder());
                    //log.info("Fuck {}", temp++)
                    //ch.pipeline().addLast(this); 由于尚不知道的原因 导致initChannel反复执行
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture future = bootstrap.connect(host, port).syncUninterruptibly();
        if (future.isSuccess()) {
            log.info("Connect to {} success", socketAddress);
        } else {
            log.error("Connect to {} failed", socketAddress, future.cause());
        }
        this.channel = future.channel();
        return future;
    }

    private class TempDecoder extends LengthFieldBasedFrameDecoder{

        private boolean added = false;
        public TempDecoder() {
            super(Integer.MAX_VALUE, Packets.FIELD_CODE_LENGTH, 4);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (!added) {
                ctx.pipeline().addLast(C_Proxy2ServerConnection.this);
                added = true;
            }
            super.channelRead(ctx, msg);
        }
    }

}
