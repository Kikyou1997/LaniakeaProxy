package com.proxy.client;

import base.*;
import base.constants.RequestCode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author kikyou
 * Created at 2020/2/16
 */
@Slf4j
public class HttpConnectionStream extends AbstractConnectionStream {

    public HttpConnectionStream(Client2ProxyConnection c2PConnection, ChannelHandlerContext context) {
        super(c2PConnection,context);
        initStream();
    }

    protected void initStream() {
        super
                // 检查是否是Http Connect请求
                .addStep(new ConnectionStep() {
                    @Override
                    public Boolean handle(Object msg, ChannelHandlerContext ctx) throws Exception {
                        ByteBuf buf = (ByteBuf) msg;
                        buf.readerIndex(0);
                        Boolean result = checkHttpHeader(buf);
                        if (steps.peek() != null) {
                            steps.peek().setLastResult(result);
                        }
                        return result;
                    }

                    private boolean checkHttpHeader(ByteBuf buf) {
                        String httpMessage = buf.readCharSequence(buf.readableBytes(), StandardCharsets.US_ASCII).toString();
                        return httpMessage.startsWith("Connect") || httpMessage.startsWith("CONNECT") || httpMessage.startsWith("connect");
                    }
                })
                // 同服务器建立连接并根据是否是Connect请求以及连接建立是否成功 回复ConnectionEstablished
                .addStep(new ConnectionStep() {
                    @Override
                    public Object handle(Object msg, ChannelHandlerContext ctx) throws Exception {
                        Boolean last = (Boolean) lastResult;
                        ByteBuf buf = (ByteBuf) msg;
                        SocketAddressEntry socketAddress = getSocketAddressFromBuf(buf, last);
                        boolean connectRequestSent = false;
                        try {
                            p2SConnection = new Proxy2ServerConnection(socketAddress, HttpConnectionStream.this);
                            byte[] hostBytes = socketAddress.getHost().getBytes(StandardCharsets.US_ASCII);
                            ByteBuf buildConnectionRequest = MessageGenerator.generateDirectBuf(RequestCode.CONNECT,
                                    Converter.convertShort2ByteArray((short) hostBytes.length), hostBytes, Converter.convertShort2ByteArray(socketAddress.getPort()));
                            connectRequestSent = p2SConnection.writeData(buildConnectionRequest).syncUninterruptibly().isSuccess();
                        } catch (Exceptions.ConnectionTimeoutException e) {
                            steps.peek().setLastResult(false);
                            return Boolean.valueOf(false);
                        }
                        boolean responseSent = false;
                        if (last) {
                            responseSent = c2PConnection.writeData(generateHttpConnectionEstablishedResponse()).syncUninterruptibly().isSuccess();
                        }
                        steps.peek().setLastResult(connectRequestSent && responseSent);
                        return Boolean.valueOf(true);
                    }

                    private SocketAddressEntry getSocketAddressFromBuf(ByteBuf buf, boolean tunnel) {
                        buf.readerIndex(0);
                        String httpMessage = buf.readCharSequence(buf.readableBytes(), StandardCharsets.US_ASCII).toString();
                        String[] strings = httpMessage.split("\n");
                        String[] host = strings[1].split(":");
                        if (host.length < 3) {
                            if (tunnel) {
                                // 这里需要调用trim是因为Http报文格式: [HttpHeader]:[blank][value] 所以需要去掉多余的空格
                                return new SocketAddressEntry(host[1].trim(), (short) 443);
                            } else {
                                return new SocketAddressEntry(host[1].trim(), (short) 80);
                            }
                        } else if (host.length == 3) {
                            return new SocketAddressEntry(host[1], Short.valueOf(host[2].trim()));
                        }
                        return null;
                    }

                    public ByteBuf generateHttpConnectionEstablishedResponse() {
                        FullHttpResponse response = new FormatHttpMessage(HttpVersion.HTTP_1_1, FormatHttpMessage.CONNECTION_ESTABLISHED);
                        String s = response.toString();
                        byte[] bytes = s.getBytes();
                        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();
                        byteBuf.writeBytes(bytes);
                        short crlf = (HttpConstants.CR << 8) | HttpConstants.LF;
                        //write crlf to end line
                        ByteBufUtil.writeShortBE(byteBuf, crlf);
                        // write crlf to mark the end of http response
                        ByteBufUtil.writeShortBE(byteBuf, crlf);
                        return byteBuf;
                    }
                })
                // 将消息发送给代理服务器 这一步应由c2p调用
                .addStep(new ConnectionStep() {
                    @Override
                    public Object handle(Object msg, ChannelHandlerContext ctx) throws Exception {
                        if (p2SConnection != null) {
                            ByteBuf encrypted = ClientContext.crypto.encrypt((ByteBuf)msg);
                            p2SConnection.writeData(encrypted);
                        }
                        return null;
                    }
                })
                // 将代理服务器返回的消息进行处理 并将处理后得到的原始数据发送给客户端 也就是说这一步应该是由p2s的doRead方法调用
                .addStep(new ConnectionStep() {
                    @Override
                    public Object handle(Object msg, ChannelHandlerContext ctx) throws Exception {
                        ByteBuf decrypted = ClientContext.crypto.decrypt((ByteBuf)msg);
                        boolean result = false;
                        int times = 0;
                        while (!result && times < 3) {
                            result = c2PConnection.writeData(decrypted).syncUninterruptibly().isSuccess();
                            times++;
                        }
                        return result;
                    }
                });
    }


}

