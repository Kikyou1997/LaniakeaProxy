package com.proxy.server;

import base.arch.AbstractConnection;
import base.arch.Clock;
import base.arch.Session;
import base.protocol.LaniakeaPacket;
import base.arch.SocketAddressEntry;
import base.constants.RequestCode;
import base.constants.ResponseCode;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * Created at 2020/1/29
 */
@Slf4j
public class S_Client2ProxyConnection extends AbstractConnection<LaniakeaPacket> {


    private Crypto crypto = null;

    @Override
    protected void doRead(ChannelHandlerContext ctx, LaniakeaPacket msg) {
        if (p2SConnection == null) {
            buildConnection2RealServer(msg);
            return;
        } else if (msg.getId() != -1){
            if (this.id != msg.getId()) {
                this.id = msg.getId();
                p2SConnection.setId(this.id);
            }
        }
        ServerContext.updateSessionLastActiveTime(this.id);
        if (msg.getCode() == RequestCode.DATA_TRANS_REQ) {
            decryptDataAndSend(msg.getContent());
        }
    }

    private void buildConnection2RealServer(LaniakeaPacket msg) {
        byte code = msg.getCode();
        this.id = msg.getId();
        this.crypto = new ServerCryptoImpl(ServerContext.getSession(this.id));
        if (code == RequestCode.CONNECT) {
            ByteBuf content = msg.getContent();
            SocketAddressEntry socketAddress = SocketAddressEntry.getFromEncryptedBuf(content, crypto);
            super.p2SConnection = new S_Proxy2ServerConnection(socketAddress, this, id);
        }
    }

    private void decryptDataAndSend(ByteBuf msg) {
        var buf = ctx.alloc().buffer(msg.readableBytes());
        msg.readBytes(buf);
        buf = crypto.decrypt(buf);
        super.p2SConnection.writeData(buf);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof SessionExpiredException) {
            ByteBuf buf = new LaniakeaPacket(ResponseCode.CONN_EXPIRED,
                    id,
                    8,
                    ctx.alloc().buffer(8).writeBytes(Clock.getTimeInBytes()))
                    .toByteBuf(ctx.alloc());
            ChannelFuture future = ctx.writeAndFlush(buf).sync();
            int count = 0;
            while (!future.isSuccess() && count < 3) {
                ctx.writeAndFlush(buf).sync();
                count++;
            }
        }
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void update() {

    }
}
