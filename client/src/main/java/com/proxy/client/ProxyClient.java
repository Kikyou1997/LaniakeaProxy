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

    private int id = -1;
    private byte[] iv = new byte[Packets.IV_LENGTH];

    @Override
    public void start() {
        getIdFromRemoteServer(Config.config.getServerAddress(), Config.config.getServerPort());
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.bind(Config.config.getBindAddress() == null ? LOCALHOST : Config.config.getBindAddress(),
                Config.config.getBindPort());
        bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(new CryptoImpl(id, iv))
                        .addLast(new HeadersPrepender.RequestHeadersPrepender(id))
                        .addLast(new Client2ProxyConnection());
            }
        });
    }

    private void getIdFromRemoteServer(String ip, int port) {
        Bootstrap b = new Bootstrap();
        b.channel(NioSocketChannel.class);
        b.group(new NioEventLoopGroup(1));
        b.handler(new SimpleChannelInboundHandler<ByteBuf>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                msg.readerIndex(Packets.MAGIC_LENGTH);
                byte code = msg.readByte();
                switch (code) {
                    case ResponseCode
                            .CLOCK_RESP:
                        long clockTime = msg.readLong();
                        ctx.channel().writeAndFlush(generateAuthRequest(clockTime));
                        break;
                    case ResponseCode.AUTH_RESP:
                        id = msg.readInt();
                        msg.readBytes(iv);
                }
            }
        });
        ChannelFuture future = b.connect(ip, port).syncUninterruptibly();
        future.channel().writeAndFlush(generateClockRequest());
        int count = 0;
        while (id == -1 && count < 10) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                log.error("Interrupted while requesting id", e);
            }
            count++;
        }
        if (id == -1) {
            throw new Exceptions.AuthenticationFailedException("Get id failed");
        }
    }


    private ByteBuf generateClockRequest() {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(Packets.MAGIC_LENGTH + Packets.CODE_LENGTH);
        buf.writeShort(Packets.MAGIC).writeByte(RequestCode.GET_CLOCK_REQ);
        return buf;
    }

    private ByteBuf generateAuthRequest(long clockTime) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(Packets.MAGIC_LENGTH + Packets.CODE_LENGTH + Packets.HASH_LENGTH);
        buf.writeShort(Packets.MAGIC).writeShort(RequestCode.AUTH_REQ).writeBytes(
                CryptoUtil.getSHA256Hash(Config.config.getSecretKey(), Converter.convertLong2ByteBigEnding(clockTime))
        );
        return buf;
    }
}