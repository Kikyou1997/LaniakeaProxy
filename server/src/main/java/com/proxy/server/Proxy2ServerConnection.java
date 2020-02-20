package com.proxy.server;

import base.*;
import base.constants.Packets;
import base.constants.ResponseCode;
import base.interfaces.Crypto;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

/**
 * @author kikyou
 * @date 2020/1/29
 */
@Slf4j
public class Proxy2ServerConnection extends AbstractConnection {

    private final Crypto crypto = new ServerCryptoImpl(super.id);

    public Proxy2ServerConnection(SocketAddressEntry entry, AbstractConnection c2PConnection, int id) {
        this.c2PConnection = c2PConnection;
        if (!buildConnection2Remote(entry)) {
            log.error("Build connection to real http server failed");
            throw new Exceptions.ConnectionTimeoutException(entry);
        }
        super.id = id;
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) {

        sendData2Client(msg);
    }

    private void sendData2Client(ByteBuf msg) {
        ByteBuf encrypted = crypto.encrypt(msg);
        int length = encrypted.readableBytes() + Packets.FIELD_CODE_LENGTH + Packets.FIELD_LENGTH_LEN;
        ByteBuf finalData = PooledByteBufAllocator.DEFAULT.buffer(length);
        finalData.writeByte(ResponseCode.DATA_TRANS_RESP).writeInt(length).writeBytes(encrypted);
        c2PConnection.writeData(finalData);
    }


    @Override
    public ChannelFuture writeData(ByteBuf data) {
        return channel.writeAndFlush(data);
    }

    protected boolean buildConnection2Remote(SocketAddressEntry socketAddress) {
        String ip = socketAddress.getHost();
        short port = socketAddress.getPort();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(new NioEventLoopGroup(1));
        bootstrap.handler(this);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIME_OUT);
        ChannelFuture future = bootstrap.connect(ip, (int) port);
        future.syncUninterruptibly();
        return future.isSuccess();
    }

    @Override
    protected void disconnect() {
        channel.close();
    }


}
