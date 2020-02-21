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
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author kikyou
 * Created 2020/2/15
 */
@Slf4j
public class ProxyClient extends AbstractProxy {

    public static void main(String[] args) throws Exception {
        new ProxyClient().start();
    }

    private static final String LOCALHOST = "127.0.0.1";
    private long time = -1;

    @Override
    public void start() {
        Config.loadSettings(true);
        getIdFromRemoteServer(Config.config.getServerAddress(), Config.config.getServerPort());
        ServerBootstrap server = new ServerBootstrap();
        server.group(new NioEventLoopGroup(Platform.processorsNumber * 2));
        server.channel(NioServerSocketChannel.class);
        server.childOption(ChannelOption.SO_KEEPALIVE, true);
        server.childOption(ChannelOption.TCP_NODELAY, true);
        server.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new C_Client2ProxyConnection());
            }
        });
        server.bind(Config.config.getBindAddress() == null ? LOCALHOST : Config.config.getBindAddress(),
                Config.config.getBindPort());
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
                    case ResponseCode.CLOCK_RESP:
                        time = msg.readLong();
                        log.info("Clock received: " + time);
                        break;
                    case ResponseCode.AUTH_RESP:
                        int id = msg.readInt();
                        byte[] iv = new byte[Packets.FIELD_IV_LENGTH];
                        msg.readBytes(iv);
                        log.info(" Id received : {} Iv received: {}", id, iv);
                        ClientContext.initContext(id, iv);
                        ctx.close();
                }
            }
        });
        Channel channel = b.connect(ip, port).channel();
        channel.writeAndFlush(generateClockRequest());
        log.info("Waiting client context initialized");
        while (time == -1) {
            try {
                Thread.currentThread().join(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        channel.writeAndFlush(generateAuthRequest(time));
        while (ClientContext.iv == null) {
            try {
                Thread.currentThread().join(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("Client Context initialized");

    }

    private ByteBuf generateClockRequest() {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(Packets.FIELD_CODE_LENGTH);
        buf.writeByte(RequestCode.GET_CLOCK_REQ);
        return buf;
    }

    private ByteBuf generateAuthRequest(long clockTime) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(Packets.FIELD_CODE_LENGTH + Packets.FIELD_HASH_LENGTH);
        buf.writeByte(RequestCode.AUTH_REQ).writeBytes(
                CryptoUtil.getSHA256Hash(Config.config.getSecretKey(), Converter.convertLong2ByteBigEnding(clockTime))
        ).writeBytes(Config.config.getUsername().getBytes(StandardCharsets.US_ASCII));
        return buf;
    }

}