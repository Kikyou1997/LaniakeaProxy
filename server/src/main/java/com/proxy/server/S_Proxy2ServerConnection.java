package com.proxy.server;

import base.arch.*;
import base.constants.RequestCode;
import base.constants.ResponseCode;
import base.interfaces.Crypto;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * @date 2020/1/29
 */
@Slf4j
public class S_Proxy2ServerConnection extends AbstractConnection<ByteBuf> {


    private final Crypto crypto;

    public S_Proxy2ServerConnection(SocketAddressEntry socketAddress, AbstractConnection c2PConnection, int id) {

        this.c2PConnection = c2PConnection;
        ChannelFuture buildSuccess = buildConnection2Remote(socketAddress);
        if (buildSuccess.isSuccess()) {
            log.debug("Build connection to {} success ", socketAddress.toString());
            super.channel = buildSuccess.channel();
        } else {
            log.debug("Build connection to {} failed ", socketAddress.toString());
            throw new Exceptions.ConnectionTimeoutException(socketAddress);
        }
        super.id = id;
        crypto = new ServerCryptoImpl(super.id);
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) {
        sendData2Client(msg);
    }

    private void sendData2Client(ByteBuf msg) {
        log.debug("Host {} Size {} ", ProxyUtil.getLocalAddressAndPortFromChannel(channel), msg.readableBytes()

        );
        log.debug("Bf Enc : " +  HexDump.dump(msg));
        ByteBuf encrypted = crypto.encrypt(msg);
        log.debug("Af Enc : " +  HexDump.dump(encrypted));
        encrypted.readerIndex(0);
        LaniakeaPacket packet = new LaniakeaPacket(ResponseCode.DATA_TRANS_RESP, super.id, encrypted.readableBytes(), encrypted);
        c2PConnection.writeData(packet);
    }

    @Override
    public ChannelFuture buildConnection2Remote(SocketAddressEntry socketAddress) {
        String ip = socketAddress.getHost();
        short port = socketAddress.getPort();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(S_Proxy2ServerConnection.this);
            }
        });
        //bootstrap.option(ChannelOption.TCP_NODELAY, true);
        //bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIME_OUT);
        return bootstrap.connect(ip, (int) port).syncUninterruptibly();
    }

    @Override
    protected void disconnect() {
        channel.close();
    }

    @Override
    public void update() {}
}
