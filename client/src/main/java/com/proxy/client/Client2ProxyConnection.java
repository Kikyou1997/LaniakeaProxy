package com.proxy.client;

import base.*;

import base.constants.RequestCode;
import base.interfaces.Crypto;
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
 * Created at 2020/1/29
 */
@Slf4j
public class Client2ProxyConnection extends AbstractConnection {

    private Crypto crypto = new CryptoImpl() {
        @Override
        public ByteBuf encrypt(ByteBuf raw) throws RuntimeException {
            ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(raw.readableBytes() - 1);
            raw.readerIndex(1);
            raw.readBytes(buf);
            try {
                CryptoUtil.encrypt(buf, AbstractHandler.idIvMap.get(id), Config.getUserSecretKeyBin(idNameMap.get(id)));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error", e);
            }
            raw.clear();
            raw.writeByte(RequestCode.CONNECT);
            raw.writeBytes(buf);
            return raw;
        }
    };

    private boolean tunnelBuilt;

    private SocketAddressEntry proxyServerAddressEntry = new SocketAddressEntry(Config.config.getServerAddress(), (short) Config.config.getServerPort());

    public Client2ProxyConnection() {
        super();
        super.c2PConnection = this;
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        boolean httpConnect = checkHttpHeader(msg);
        if (httpConnect && p2SConnection != null) {
            channel.writeAndFlush(generateHttpConnectionEstablishedResponse());
        }
        if (!tunnelBuilt || !p2SConnection.isChannelActive()) {
            tunnelBuilt = buildTunnel(msg, httpConnect);
            return;
        }
        if (p2SConnection != null) {
            log.info("Original Size: " + msg.readableBytes());
            ByteBuf encrypted = ClientContext.crypto.encrypt(msg);
            log.info("Encrypted Size: " + encrypted.readableBytes());
            p2SConnection.writeData(encrypted);
        }
    }


    private boolean buildTunnel(ByteBuf msg, boolean httpConnect) {
        msg.readerIndex(0);
        SocketAddressEntry socketAddress = getSocketAddressFromBuf(msg, httpConnect);
        boolean connectRequestSent = false;
        try {
            p2SConnection = new Proxy2ServerConnection(proxyServerAddressEntry, this);
            byte[] hostBytes = socketAddress.getHost().getBytes(StandardCharsets.US_ASCII);
            ByteBuf buildConnectionRequest = MessageGenerator.generateDirectBuf(RequestCode.CONNECT, Converter.convertInteger2ByteBigEnding(id),
                    Converter.convertShort2ByteArray((short) hostBytes.length), hostBytes, Converter.convertShort2ByteArray(socketAddress.getPort()));
            crypto.encrypt(buildConnectionRequest);
            connectRequestSent = p2SConnection.writeData(buildConnectionRequest).syncUninterruptibly().isSuccess();
        } catch (Exceptions.ConnectionTimeoutException e) {
            log.error("Connect to remote proxy server timeout");
        }
        boolean responseSent = false;
        if (httpConnect) {
            responseSent = c2PConnection.writeData(generateHttpConnectionEstablishedResponse()).syncUninterruptibly().isSuccess();
        }
        return connectRequestSent && responseSent;
    }

    private boolean checkHttpHeader(ByteBuf buf) {
        String httpMessage = buf.readCharSequence(buf.readableBytes(), StandardCharsets.US_ASCII).toString();
        return httpMessage.startsWith("Connect") || httpMessage.startsWith("CONNECT") || httpMessage.startsWith("connect");
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

    @Override
    public ChannelFuture writeData(ByteBuf data) {
        return channel.writeAndFlush(data);
    }


}
