package com.proxy.client;

import base.arch.*;
import base.constants.Packets;
import base.constants.RequestCode;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.sql.Ref;

/**
 * @author kikyou
 * Created at 2020/1/29
 */
@Slf4j
public class C_Client2ProxyConnection extends AbstractConnection<ByteBuf> {

    private Crypto crypto = ClientContext.crypto;

    private boolean tunnelBuilt;

    private boolean tunnelNeeded;

    private boolean isChecked;

    private SocketAddressEntry realTargetHost = null;


    private SocketAddressEntry proxyServerAddressEntry = new SocketAddressEntry(Config.config.getServerAddress(), (short) Config.config.getServerPort());

    public C_Client2ProxyConnection() {
        super.c2PConnection = this;
        super.id = ClientContext.id;
    }


    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if (!isChecked && !tunnelBuilt) {
            tunnelBuilt = buildTunnel(msg, checkHttpHeaderAndGetSocketAddress(msg));
            if (tunnelNeeded && tunnelBuilt) {
                return;
            }
        }
        if (p2SConnection != null) {
            msg.readerIndex(0);
            var encrypted = crypto.encrypt(msg);
            LaniakeaPacket packet = new LaniakeaPacket(RequestCode.DATA_TRANS_REQ, id, encrypted.readableBytes(), encrypted);
            p2SConnection.writeData(packet);
        }
    }

    private boolean buildTunnel(ByteBuf msg, boolean httpConnect) {
        msg.readerIndex(0);
        SocketAddressEntry socketAddress = this.realTargetHost;
        boolean connectRequestSent = false;
        try {
            p2SConnection = new C_Proxy2ServerConnection(proxyServerAddressEntry, this);
            p2SConnection.buildConnection2Remote(proxyServerAddressEntry);
            byte[] hostBytes = socketAddress.getHost().getBytes(StandardCharsets.US_ASCII);
            ByteBuf buf = ctx.alloc().buffer(hostBytes.length + Packets.FILED_PORT_LENGTH + Packets.FILED_HOST_LENGTH);
            buf.writeBytes(hostBytes);
            buf.writeShort(socketAddress.getPort());
            buf = crypto.encrypt(buf);
            byte[] encrypted = ProxyUtil.getBytesFromByteBuf(buf);
            ByteBuf buildConnectionRequest = MessageGenerator.generateDirectBuf(RequestCode.CONNECT,
                    Converter.convertInteger2ByteBigEnding(id), Converter.convertInteger2ByteBigEnding(buf.readableBytes()),
                    encrypted);
            connectRequestSent = p2SConnection.writeData(buildConnectionRequest).syncUninterruptibly().isSuccess();
            ReferenceCountUtil.safeRelease(buf);
        } catch (Exceptions.ConnectionTimeoutException e) {
            log.error("Connect to remote proxy server timeout", e);
        }
        boolean responseSent = false;
        if (httpConnect) {
            responseSent = c2PConnection.writeData(generateHttpConnectionEstablishedResponse()).syncUninterruptibly().isSuccess();
        }
        return connectRequestSent && responseSent;
    }

    private boolean checkHttpHeaderAndGetSocketAddress(ByteBuf buf) {
        buf.readerIndex(0);
        String httpMessage = buf.readCharSequence(buf.readableBytes(), StandardCharsets.US_ASCII).toString();
        log.debug("Http message : {}", httpMessage);
        boolean isConnect = httpMessage.startsWith("Connect") || httpMessage.startsWith("CONNECT") || httpMessage.startsWith("connect");
        if (!isChecked) {
            this.isChecked = true;
            this.tunnelNeeded = isConnect;
        }
        if (this.realTargetHost != null) {
            return isConnect;
        }
        buf.readerIndex(0);
        String[] strings = httpMessage.split("\r\n");
        for (String s : strings) {
            if (s.length() > 4 && s.substring(0, 4).equalsIgnoreCase("Host")) {
                String[] host = s.split(":");
                host[1] = host[1].trim();
                if (host.length < 3) {
                    if (isConnect) {
                        // 这里需要调用trim是因为Http报文格式: [HttpHeader]:[blank][value] 所以需要去掉多余的空格
                        this.realTargetHost = new SocketAddressEntry(host[1], (short) 443);
                    } else {
                        this.realTargetHost = new SocketAddressEntry(host[1], (short) 80);
                    }
                } else if (host.length == 3) {
                    this.realTargetHost = new SocketAddressEntry(host[1], Short.valueOf(host[2].trim()));
                }
                break;
            }
        }
        if (this.realTargetHost == null){
            log.debug("No host found, original message: {}", httpMessage);
        }
        return isConnect;
    }

    private ByteBuf generateHttpConnectionEstablishedResponse() {
        FullHttpResponse response = new FormatHttpMessage(HttpVersion.HTTP_1_1, FormatHttpMessage.CONNECTION_ESTABLISHED);
        String s = response.toString();
        byte[] bytes = s.getBytes();
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(bytes);
        short crlf = (HttpConstants.CR << 8) | HttpConstants.LF;
        //write crlf to end line
        ByteBufUtil.writeShortBE(byteBuf, crlf);
        // write crlf to mark the end of http response
        ByteBufUtil.writeShortBE(byteBuf, crlf);
        return byteBuf;
    }

}
