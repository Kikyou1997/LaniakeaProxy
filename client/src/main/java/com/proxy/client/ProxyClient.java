package com.proxy.client;

import base.arch.*;
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
import org.apache.commons.cli.*;

import java.nio.charset.StandardCharsets;

/**
 * @author kikyou
 * Created 2020/2/15
 */
@Slf4j
public class ProxyClient extends AbstractProxy {

    public static void main(String[] args) throws Exception {
        new ProxyClient().prepare(args).start();
    }

    private static final String LOCALHOST = "127.0.0.1";
    private long serverClock = -1;

    @Override
    protected AbstractProxy prepare(String[] args) {
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        options.addOption(null, super.configOption, true, null);
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption(super.configOption)) {
                Config.CLIENT_CONFIG_FILE_PATH = commandLine.getOptionValue(super.configOption);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return this;
    }

    @Override
    public void start() {
        Config.loadSettings(true);
        getIdFromRemoteServer(Config.config.getServerAddress(), Config.config.getServerPort());
        ServerBootstrap server = new ServerBootstrap();
        server.group(new NioEventLoopGroup(Platform.coreNum), new NioEventLoopGroup(Platform.coreNum * 2));
        server.channel(NioServerSocketChannel.class);
        server.childOption(ChannelOption.TCP_NODELAY, true);
        server.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(new CustomizedIdleConnectionHandler())
                        .addLast(new C_Client2ProxyConnection());
            }
        });
        server.bind(Config.config.getBindAddress() == null ? LOCALHOST : Config.config.getBindAddress(),
                Config.config.getLocalPort());
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
                        serverClock = msg.readLong();
                        log.info("Clock received: " + serverClock);
                        break;
                    case ResponseCode.AUTH_RESP:
                        int id = msg.readInt();
                        log.info(" Id received : {}", id);
                        ClientContext.initContext(id);
                        ctx.close();
                        break;
                    case ResponseCode.AUTH_FAILED:
                        log.error("username or secret key incorrect");
                        System.exit(-1);
                    default:
                        //do nothing
                        break;
                }
            }
        });
        Channel channel = b.connect(ip, port).channel();
        channel.writeAndFlush(generateClockRequest());
        log.info("Waiting client context initialized");
        int waitingTime = 5000;
        while (serverClock == -1 && waitingTime > 0) {
            try {
                channel.writeAndFlush(generateClockRequest());
                Thread.currentThread().join(50);
                waitingTime -= 50;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (serverClock == -1) {
            log.error("Build Connection failed");
            System.exit(-1);
        }
        ChannelFuture future = channel.writeAndFlush(generateAuthRequest(serverClock)).syncUninterruptibly();
        if (!future.isSuccess()) {
            log.error("Build Connection failed");
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
        buf.
                writeByte(RequestCode.AUTH_REQ).
                writeBytes(
                CryptoUtil.getSHA256Hash(Config.config.getSecretKey(), Converter.convertLong2ByteBigEnding(clockTime))).
                writeBytes(ClientContext.iv).
                writeBytes(Config.config.getUsername().getBytes(StandardCharsets.US_ASCII));
        return buf;
    }

}