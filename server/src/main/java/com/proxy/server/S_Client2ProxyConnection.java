package com.proxy.server;

import base.arch.AbstractConnection;
import base.arch.LaniakeaPacket;
import base.arch.SocketAddressEntry;
import base.constants.Packets;
import base.constants.RequestCode;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author kikyou
 * Created at 2020/1/29
 */
@Slf4j
public class S_Client2ProxyConnection extends AbstractConnection<LaniakeaPacket> {

    private int id;

    private Crypto crypto = null;


    @Override
    protected void doRead(ChannelHandlerContext ctx, LaniakeaPacket msg) {
        if (p2SConnection == null) {
            buildConnection2RealServer(msg);
            return;
        }
        if (msg.getCode() == RequestCode.DATA_TRANS_REQ) {
            decryptDataAndSend(msg.getContent());
        }
    }

    private void buildConnection2RealServer(LaniakeaPacket msg) {
        byte code = msg.getCode();
        this.id = msg.getId();
        int length = msg.getLength();
        crypto = new ServerCryptoImpl(id);
        if (code == RequestCode.CONNECT) {
            var buf = crypto.decrypt(msg.getContent());
            SocketAddressEntry socketAddress = getHostFromBuf(buf);
            super.p2SConnection = new S_Proxy2ServerConnection(socketAddress, this, id);
        }
    }

    private void decryptDataAndSend(ByteBuf msg) {
        var buf = ctx.alloc().buffer(msg.readableBytes());
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
