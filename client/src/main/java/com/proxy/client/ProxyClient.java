package com.proxy.client;

import base.*;
import base.constants.Packets;
import base.constants.RequestCode;
import base.constants.ResponseCode;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * Created 2020/2/15
 */
@Slf4j
public class ProxyClient extends AbstractProxy {

    public static void main(String[] args) throws Exception {

    }

    private static final String LOCALHOST = "127.0.0.1";


    @Override
    public void start() {
        getIdFromRemoteServer(Config.config.getServerAddress(), Config.config.getServerPort());
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.bind(Config.config.getBindAddress() == null ? LOCALHOST : Config.config.getBindAddress(),
                Config.config.getBindPort());
        bootstrap.childHandler(new Client2ProxyConnection());
    }

    private void getIdFromRemoteServer(String ip, int port) {
        Bootstrap b = new Bootstrap();
        b.channel(NioSocketChannel.class);
        b.group(new NioEventLoopGroup(1));
        b.handler(new SimpleChannelInboundHandler<ByteBuf>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                byte code = msg.readByte();
                switch (code) {
                    case ResponseCode
                            .CLOCK_RESP:
                        long clockTime = msg.readLong();
                        ctx.channel().writeAndFlush(generateAuthRequest(clockTime));
                        break;
                    case ResponseCode.AUTH_RESP:
                        int id = msg.readInt();
                        byte[] iv = new byte[Packets.FILED_IV_LENGTH];
                        msg.readBytes(iv);
                        ClientContext.initContext(id, iv);
                }
            }
        });
        ChannelFuture future = b.connect(ip, port).syncUninterruptibly();
        future.channel().writeAndFlush(generateClockRequest());
        int count = 0;
        while (ClientContext.id == -1 && count < 10) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                log.error("Interrupted while requesting id", e);
            }
            count++;
        }
        if (ClientContext.id == -1) {
            throw new Exceptions.AuthenticationFailedException("Get id failed");
        }
    }

    private ByteBuf generateClockRequest() {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(Packets.FILED_CODE_LENGTH);
        buf.writeByte(RequestCode.GET_CLOCK_REQ);
        return buf;
    }

    private ByteBuf generateAuthRequest(long clockTime) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(Packets.FILED_CODE_LENGTH + Packets.FILED_HASH_LENGTH);
        buf.writeShort(RequestCode.AUTH_REQ).writeBytes(
                CryptoUtil.getSHA256Hash(Config.config.getSecretKey(), Converter.convertLong2ByteBigEnding(clockTime))
        );
        return buf;
    }
}