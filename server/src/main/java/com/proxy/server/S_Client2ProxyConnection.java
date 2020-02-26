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
public class S_Client2ProxyConnection extends AbstractConnection {

    private int id;

    private Crypto crypto = null;

    private static int instanceCount = 0;


    public S_Client2ProxyConnection() {
        log.info("instance count:{}", ++instanceCount);
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) {
        if (p2SConnection == null) {
            buildConnection2RealServer(msg);
            return;
        }
        msg.readerIndex(0);
        if (msg.readByte() == RequestCode.DATA_TRANS_REQ) {
            msg.readerIndex(0);
            decryptDataAndSend(msg);
        }
    }

    private void buildConnection2RealServer(ByteBuf msg) {
        msg.readerIndex(0);
        byte code = msg.readByte();
        this.id = msg.readInt();
        int length = msg.readInt();
        crypto = new ServerCryptoImpl(id);
        if (code == RequestCode.CONNECT) {
            ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(msg.readableBytes());
            msg.readBytes(buf);
            buf = crypto.decrypt(buf);
            SocketAddressEntry socketAddress = getHostFromBuf(buf);
            super.p2SConnection = new S_Proxy2ServerConnection(socketAddress, this, id);
        }
    }

    private void decryptDataAndSend(ByteBuf msg) {
        msg.readerIndex(Packets.HEADERS_DATA_REQ_LEN);
        var buf = PooledByteBufAllocator.DEFAULT.buffer(msg.readableBytes());
        msg.readBytes(buf);
        buf = crypto.decrypt(buf);
        super.p2SConnection.writeData(buf).syncUninterruptibly();
    }

    private SocketAddressEntry getHostFromBuf(ByteBuf buf) {
        String host = buf.readCharSequence(buf.readableBytes() - Packets.FILED_PORT_LENGTH, StandardCharsets.US_ASCII).toString();
        short port = buf.readShort();
        return new SocketAddressEntry(host, port);
    }

}
