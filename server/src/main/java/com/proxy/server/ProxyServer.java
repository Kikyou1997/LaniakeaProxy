package com.proxy.server;

import base.AbstractProxy;
import base.Config;
import base.CryptoUtil;
import base.Platform;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import static base.Config.config;

/**
 * @author kikyou
 * Created 2020/2/15
 */
public class ProxyServer extends AbstractProxy {

    @Override
    public void start() {
        Config.loadSettings(false);
        ServerBootstrap server = new ServerBootstrap();
        server.group(new NioEventLoopGroup(Platform.processorsNumber * 2));
        server.channel(NioServerSocketChannel.class);
        server.childOption(ChannelOption.TCP_NODELAY, true);
        server.childOption(ChannelOption.SO_KEEPALIVE, true);
        server.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(new MessageProcessor())
                        .addLast(new CustomizedLengthBasedDecoder())
                        .addLast(new Client2ProxyConnection());
            }
        });
        server.bind(config.getServerAddress() == null ? "0.0.0.0" :
                config.getServerAddress(), config.getServerPort()).syncUninterruptibly();
    }

    public static void main(String[] args) throws Exception {
        new ProxyServer().start();
    }
}