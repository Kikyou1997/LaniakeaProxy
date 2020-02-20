package com.proxy.server;

import base.*;
import base.constants.Packets;
import base.constants.RequestCode;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author kikyou
 * Created at 2020/1/29
 */
@Slf4j
public class Client2ProxyConnection extends AbstractConnection {

    private int id;

    private Crypto crypto = null;

    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) {
        log.info(HexDump.dump(msg));
        if (p2SConnection == null) {
            buildConnection2RealServer(msg);
            return;
        }

        decryptDataAndSend(msg);
    }

    private void buildConnection2RealServer(ByteBuf msg) {
        msg.readerIndex(0);
        byte code = msg.readByte();
        this.id = msg.readInt();
        crypto = new ServerCryptoImpl(id);
        if (code == RequestCode.CONNECT) {
            ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(msg.readableBytes());
            msg.readBytes(buf);
            buf = crypto.decrypt(buf);
            SocketAddressEntry socketAddress = getHostFromBuf(buf);
            super.p2SConnection = new Proxy2ServerConnection(socketAddress, this, id);
        }
    }

    private void decryptDataAndSend(ByteBuf msg) {
        msg.readerIndex(Packets.HEADERS_DATA_REQ_LEN);
        var buf = PooledByteBufAllocator.DEFAULT.buffer(msg.readableBytes());
        msg.readBytes(buf);
        log.info("Before dec: {}", buf.readableBytes());
        buf = crypto.decrypt(buf);
        log.info("After decrypted: {}" ,buf.readableBytes());
        super.p2SConnection.writeData(buf).syncUninterruptibly();
    }

    private SocketAddressEntry getHostFromBuf(ByteBuf buf) {
        short length = buf.readShort();
        String host = buf.readCharSequence(length, StandardCharsets.US_ASCII).toString();
        short port = buf.readShort();
        return new SocketAddressEntry(host, port);
    }


    @Override
    public ChannelFuture writeData(ByteBuf data) {
        return channel.writeAndFlush(data);
    }
}
