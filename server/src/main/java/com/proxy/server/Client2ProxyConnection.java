package com.proxy.server;

import base.*;
import base.constants.RequestCode;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

/**
 * @author kikyou
 * Created at 2020/1/29
 */
public class Client2ProxyConnection extends AbstractConnection {

    private int id;

    private final Crypto crypto = new CryptoImpl() {
        @Override
        public ByteBuf encrypt(ByteBuf raw) throws RuntimeException {
            byte[] secretKey = Config.getUserSecretKeyBin(AbstractHandler.idNameMap.get(id));
            byte[] iv = AbstractHandler.idIvMap.get(id);
            try {
                return CryptoUtil.encrypt(raw, secretKey, iv);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    };

    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) {
        if (p2SConnection == null) {
            buildConnection2RealServer(msg);
        }
        decryptDataAndSend(msg);
    }

    private void buildConnection2RealServer(ByteBuf msg) {
        ByteBuf buf = msg;
        buf.readerIndex(1);
        this.id = buf.readInt();
        buf.readerIndex(0);
        byte code = buf.readByte();
        if (code == RequestCode.CONNECT) {
            crypto.decrypt(buf);
            SocketAddressEntry socketAddress = getHostFromBuf(buf);
            super.p2SConnection = new Proxy2ServerConnection(socketAddress, this, id);
        }
    }

    private void decryptDataAndSend(ByteBuf msg) {
        ByteBuf buf = crypto.decrypt(msg);
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
