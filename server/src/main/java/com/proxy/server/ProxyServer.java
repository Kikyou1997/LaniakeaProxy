package com.proxy.server;

import base.*;
import base.constants.Packets;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import static base.Config.config;

/**
 * @author kikyou
 * Created 2020/2/15
 */
public class ProxyServer extends AbstractProxy {

    @Override
    public void start() {
        System.out.println(CryptoUtil.encodeFromBytes(CryptoUtil.initKey()));
        Config.loadSettings(false);
        ServerBootstrap server = new ServerBootstrap();
        server.group(new NioEventLoopGroup(Platform.processorsNumber), new NioEventLoopGroup(Platform.processorsNumber * 2));
        server.channel(NioServerSocketChannel.class);
        server.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(new CustomizedIdleConnectionHandler())
                        .addLast(new MessageProcessor("processor"))
                        .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, Packets.FIELD_ID_LENGTH + Packets.FIELD_CODE_LENGTH, Packets.FIELD_LENGTH_LEN))
                        .addLast(new S_Client2ProxyConnection());
            }
        });
        server.bind(config.getServerAddress() == null ? "0.0.0.0" :
                config.getServerAddress(), config.getServerPort()).syncUninterruptibly();
    }

    public static void main(String[] args) throws Exception {
        new ProxyServer().start();
    }
}