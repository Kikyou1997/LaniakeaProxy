package com.proxy.server;

import base.AbstractProxy;
import base.Config;
import base.constants.Packets;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import static base.Config.config;

/**
 * @author kikyou
 * Created 2020/2/15
 */
public class ProxyServer extends AbstractProxy {

    @Override
    public void start() {
        ServerBootstrap server = new ServerBootstrap();
        server.childOption(ChannelOption.TCP_NODELAY, true);
        server.bind(config.getServerAddress() == null ? "0.0.0.0" :
                config.getServerAddress(), config.getServerPort());
        server.childHandler(new ChannelInitializer<NioSctpChannel>() {

            @Override
            protected void initChannel(NioSctpChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(new MessageProcessor())
                        .addLast(new CustomizedLengthBasedDecoder())
                        .addLast(new Client2ProxyConnection());
            }
        });

    }

    public static void main(String[] args) throws Exception {

    }
}