package com.proxy.client;

import base.arch.*;
import base.constants.Packets;
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
public class C_Proxy2ServerConnection extends AbstractConnection {

    //private static int temp = 0;


    public C_Proxy2ServerConnection() {
    }

    public C_Proxy2ServerConnection(SocketAddressEntry entry, AbstractConnection c2PConnection) throws Exceptions.ConnectionTimeoutException {
        super.c2PConnection = c2PConnection;
    }

    @Override
<<<<<<< HEAD
    protected void doRead(ChannelHandlerContext ctx, LaniakeaPacket msg) throws Exception {
        ByteBuf decrypted = ClientContext.crypto.decrypt(msg.getContent());
        c2PConnection.writeData(decrypted);
=======
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        ByteBuf buf =  ctx.alloc().buffer(msg.readableBytes() - Packets.HEADERS_DATA_RESP_LEN);
        msg.readerIndex(Packets.HEADERS_DATA_RESP_LEN);
        msg.readBytes(buf);
        buf.readerIndex(0);
        ByteBuf decrypted = ClientContext.crypto.decrypt(buf);
        log.debug("Dec:{} host: {}", HexDump.dump(decrypted), ProxyUtil.getRemoteAddressAndPortFromChannel(channel));
        c2PConnection.writeData(decrypted).syncUninterruptibly().isSuccess();
>>>>>>> parent of 146860a... encode&decode finished
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

<<<<<<< HEAD
                    ch.pipeline()
                            .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 5, 4))
                            .addLast(new DataTransmissionPacketDecoder())
                            .addLast(C_Proxy2ServerConnection.this)

=======
                    ch.pipeline().addLast(new TempDecoder())
>>>>>>> parent of 146860a... encode&decode finished
                            .addLast(new DataTransmissionPacketEncoder());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //bootstrap.option(ChannelOption.TCP_NODELAY, true);
        ChannelFuture future = bootstrap.connect(host, port).syncUninterruptibly();
        if (!future.isSuccess()) {
            this.channel.close();
        }
        return future;
    }

<<<<<<< HEAD
=======
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

>>>>>>> parent of 146860a... encode&decode finished
}
