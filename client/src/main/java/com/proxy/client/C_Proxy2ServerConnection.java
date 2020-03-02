package com.proxy.client;

import base.arch.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * @date 2020/1/29
 */
@Slf4j
public class C_Proxy2ServerConnection extends AbstractConnection<LaniakeaPacket> {

    //private static int temp = 0;

    public C_Proxy2ServerConnection(SocketAddressEntry entry, AbstractConnection c2PConnection) throws Exceptions.ConnectionTimeoutException {
        super.c2PConnection = c2PConnection;
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, LaniakeaPacket msg) throws Exception {
        ByteBuf decrypted = ClientContext.crypto.decrypt(msg.getContent());

        c2PConnection.writeData(decrypted);
    }


    @Override
    public ChannelFuture buildConnection2Remote(SocketAddressEntry socketAddress) {
        String host = socketAddress.getHost().trim();
        short port = socketAddress.getPort();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                try {

                    ch.pipeline()
                            .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 5, 4))
                            .addLast(new DataTransmissionPacketDecoder())
                            .addLast(new DataTransmissionPacketEncoder())
                            .addLast(C_Proxy2ServerConnection.this);
                    //log.info("Fuck {}", temp++)
                    //ch.pipeline().addLast(this); 由于尚不知道的原因 导致initChannel反复执行
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //bootstrap.option(ChannelOption.TCP_NODELAY, true);
        ChannelFuture future = bootstrap.connect(host, port).syncUninterruptibly();
        this.channel = future.channel();
        if (!future.isSuccess()) {
            this.channel.close();
        }
        return future;
    }



}
