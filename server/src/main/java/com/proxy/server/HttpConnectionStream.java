package com.proxy.server;

import base.*;
import base.constants.Packets;
import base.constants.RequestCode;
import base.constants.ResponseCode;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

/**
 * @author kikyou
 * Created at 2020/2/18
 */
public class HttpConnectionStream extends AbstractConnectionStream {

    private int id;

    private final Crypto crypto = new CryptoImpl(){
        @Override
        public ByteBuf encrypt(ByteBuf raw) throws RuntimeException {
            byte[] secretKey = Config.getUserSecretKeyBin(AbstractHandler.idNameMap.get(id));
            byte[] iv = AbstractHandler.idIvMap.get(id);
            try{
                return CryptoUtil.encrypt(raw, secretKey, iv);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    };

    public HttpConnectionStream(AbstractConnection c2PConnection, ChannelHandlerContext context) {
        super(c2PConnection, context);
    }

    @Override
    protected void initStream() {
        super.
                // 和真正的服务器建立tcp连接
                addStep(new ConnectionStep() {

                    private ConnectionStep dataTransferStep = HttpConnectionStream.this.peek();

                    @Override
                    public Object handle(Object msg, ChannelHandlerContext ctx) throws RuntimeException {
                        ByteBuf buf= (ByteBuf)msg;
                        buf.readerIndex(1);
                        HttpConnectionStream.this.id = buf.readInt();
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
                        ByteBuf buf = (ByteBuf)msg;
                        ByteBuf encrypted = crypto.encrypt(buf);
                        int length = encrypted.readableBytes() + Packets.FIELD_CODE_LENGTH + Packets.FIELD_LENGTH_LEN;
                        ByteBuf finalData = PooledByteBufAllocator.DEFAULT.buffer(length);
                        finalData.writeByte(ResponseCode.DATA_TRANS_RESP).writeInt(length).writeBytes(encrypted);
                        c2PConnection.writeData(finalData);
                        return null;
                    }
                });
    }
}
