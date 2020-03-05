package com.proxy.server;

import base.arch.*;
import base.constants.Packets;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import org.apache.commons.cli.*;

import static base.arch.Config.config;

/**
 * @author kikyou
 * Created 2020/2/15
 */
public class ProxyServer extends AbstractProxy {

    @Override
    protected AbstractProxy prepare(String[] args) {
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        options.addOption(null, super.configOption, true, null);
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption(super.configOption)) {
                Config.SERVER_CONFIG_FILE_PATH = commandLine.getOptionValue(super.configOption);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return this;
    }

    @Override
    public void start() {
        System.out.println(CryptoUtil.encodeFromBytes(CryptoUtil.initKey()));
        Config.loadSettings(false);
        ServerBootstrap server = new ServerBootstrap();
        server.group(new NioEventLoopGroup(Platform.coreNum), new NioEventLoopGroup(Platform.coreNum * 2));
        server.channel(NioServerSocketChannel.class);
        server.childOption(ChannelOption.TCP_NODELAY, true);
        server.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                MessageProcessor messageProcessor = new MessageProcessor();
                ChannelTrafficShapingHandler channelTrafficStatistic = new ChannelTrafficShapingHandler(0);
                ch.pipeline()
                        //.addLast(new CustomizedIdleConnectionHandler())
                        .addLast(messageProcessor)
                        .addLast(channelTrafficStatistic)
                        .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                                Packets.FIELD_ID_LENGTH + Packets.FIELD_CODE_LENGTH, Packets.FIELD_LENGTH_LEN))
                        .addLast(new DataTransmissionPacketDecoder())
                        .addLast(new StatisticHandler(channelTrafficStatistic.trafficCounter()))
                        .addLast(new DataTransmissionPacketEncoder())
                        .addLast(new StatisticHandler(channelTrafficStatistic.trafficCounter()))
                        .addLast(new S_Client2ProxyConnection());
            }
        });
        server.bind(config.getServerAddress() == null ? "0.0.0.0" :
                config.getServerAddress(), config.getServerPort()).syncUninterruptibly();
    }

    public static void main(String[] args) throws Exception {
        new ProxyServer().prepare(args).start();
    }
}