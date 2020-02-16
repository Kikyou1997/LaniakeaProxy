package com.proxy.client;

import base.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
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

    public HttpConnectionStream(Client2ProxyConnection c2PConnection) {
        super(c2PConnection);
        initStream();
    }

    protected void initStream() {
        // 检查是否是Http Connect请求
        addSteps(new ConnectionStep() {
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
                .addSteps(new ConnectionStep() {
                    @Override
                    public Object handle(Object msg, ChannelHandlerContext ctx) throws Exception {
                        Boolean last = (Boolean) lastResult;
                        ByteBuf buf = (ByteBuf) msg;
                        SocketAddressEntry socketAddress = getSocketAddressFromBuf(buf, last);
                        try {
                            p2SConnection = new Proxy2ServerConnection(socketAddress);

                        } catch (Exceptions.ConnectionTimeoutException e) {
                            steps.peek().setLastResult(false);
                            return Boolean.valueOf(false);
                        }
                        ChannelFuture future = null;
                        if (last) {
                            future = c2PConnection.writeData(generateHttpConnectionEstablishedResponse()).syncUninterruptibly();
                        }
                        steps.peek().setLastResult(future == null || future.isSuccess());
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
                        int crlf = (HttpConstants.CR << 8) | HttpConstants.LF;
                        //write crlf to end line
                        ByteBufUtil.writeShortBE(byteBuf, crlf);
                        // write crlf to mark the end of http response
                        ByteBufUtil.writeShortBE(byteBuf, crlf);
                        return byteBuf;
                    }
                });
    }


}

}
