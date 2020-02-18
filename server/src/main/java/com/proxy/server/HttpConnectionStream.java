package com.proxy.server;

import base.AbstractConnection;
import base.AbstractConnectionStream;
import base.CryptoImpl;
import base.SocketAddressEntry;
import base.constants.RequestCode;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

/**
 * @author kikyou
 * Created at 2020/2/18
 */
public class HttpConnectionStream extends AbstractConnectionStream {

    private final Crypto crypto = new CryptoImpl();

    public HttpConnectionStream(AbstractConnection c2PConnection, ChannelHandlerContext context) {
        super(c2PConnection, context);
    }

    @Override
    protected void initStream() {
        super.
                // 和真正的服务器建立tcp连接
                addStep(new ConnectionStep() {

                    private ConnectionStep dataTransferStep = HttpConnectionStream.this.currentStep();

                    @Override
                    public Object handle(Object msg, ChannelHandlerContext ctx) throws RuntimeException {
                        ByteBuf buf= (ByteBuf)msg;
                        buf.readerIndex(0);
                        byte code = buf.readByte();
                        if (code == RequestCode.CONNECT) {
                            crypto.decrypt(buf);
                            SocketAddressEntry socketAddress = getHostFromBuf(buf);
                            HttpConnectionStream.super.p2SConnection = new Proxy2ServerConnection(socketAddress, HttpConnectionStream.this);
                        } else {
                            dataTransferStep.handle(msg, ctx);
                        }
                        return null;
                    }
                    private SocketAddressEntry getHostFromBuf(ByteBuf buf) {
                        short length = buf.readShort();
                        String host =  buf.readCharSequence(length, StandardCharsets.US_ASCII).toString();
                        short port = buf.readShort();
                        return new SocketAddressEntry(host, port);
                    }

                })
                // 解密数据并转发到真正的服务器
                .addStep(new ConnectionStep() {
                    @Override
                    public Object handle(Object msg, ChannelHandlerContext ctx) throws RuntimeException {
                        ByteBuf buf = crypto.decrypt((ByteBuf) msg);
                        HttpConnectionStream.super.p2SConnection.writeData(buf).syncUninterruptibly();
                        return null;
                    }
                })
                // 发送报文到代理客户端
                .addStep(new ConnectionStep() {
                    @Override
                    public Object handle(Object msg, ChannelHandlerContext ctx) throws RuntimeException {
                        c2PConnection.writeData((ByteBuf)msg);
                        return null;
                    }
                });
    }
}
