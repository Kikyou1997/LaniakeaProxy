import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author kikyou
 */
@Slf4j
public class ProxyUtil {

    private static FullHttpResponse requestFailed;

    public static FullHttpResponse buildFailureResponse() {
        if (requestFailed == null) {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST,
                    Unpooled.wrappedBuffer("<h>Request failed</h>".getBytes()));
            return response;
        } else {
            return requestFailed;
        }
    }

    public static InetAddress getAddressFromChannel(Channel channel) {
        return ((InetSocketAddress) channel.remoteAddress()).getAddress();
    }

    public static String getRemoteAddressInStringFormatFromChannel(Channel channel) {
        return ((InetSocketAddress) channel.remoteAddress()).getAddress().toString();
    }

    public static String getRemoteAddressAndPortFromChannel(Channel channel) {
        try {
            String host = ((InetSocketAddress) channel.remoteAddress()).getAddress().toString();
            int port = ((InetSocketAddress) channel.remoteAddress()).getPort();
            return host + ":" + port;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getRemoteAddressAndPortFromChannel(ChannelHandlerContext ctx) {
        return getRemoteAddressAndPortFromChannel(ctx.channel());
    }

    public static String getLocalAddressAndPortFromChannel(Channel channel) {
        String host = ((InetSocketAddress) channel.localAddress()).getAddress().toString();
        int port = ((InetSocketAddress) channel.localAddress()).getPort();
        return host + ":" + port;
    }

    public static String getLocalAddressAndPortFromChannel(ChannelHandlerContext ctx) {
        return getRemoteAddressAndPortFromChannel(ctx.channel());
    }

    public static String getHostFromHttpMessage(String httpMessage) { // will use regex later
        for (String s : httpMessage.split("\n")) {
            if (httpMessage.startsWith("Connect") || httpMessage.startsWith("CONNECT") || httpMessage.startsWith("connect")) {
                String temp[] = s.split(" ");
                return temp[1];
            }
            if (s.startsWith("HOST") || (s.startsWith("host")) || s.startsWith("Host")) {
                String mid[] = s.split(":");
                if (mid.length < 3)
                    return mid[1].trim();
                else return (mid[1] + ":" + mid[2]).trim();
            }
        }


        return null;
    }


    public static byte[] getBytesFromByteBuf(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(0, bytes);
        return bytes;
    }

    public static byte[] readAllBytesFromByteBuf(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes, 0, byteBuf.readableBytes());
        return bytes;
    }

    public static byte[] getBytesFromByteBuf(Object o) {
        if (o instanceof ByteBuf) return getBytesFromByteBuf((ByteBuf) o);
        return null;
    }

    public static ByteBuf getByteBufFromBytes(byte[] bytes) {
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeBytes(bytes);
        return byteBuf;
    }

    /**
     * 现在感觉其实这个方法不该这么写,完全可以定义一个ConnectionEstablishedResponse的常量
     *
     * @param channel
     */
    public static void sendConnectionEstablishedResponse(Channel channel) {
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
        if (ControlCentre.auth) {
            // used to padding payload to 272 bytes, because the fucking netty FixedLengthFrameDecoder will not pass packet to next handler until
            //it has collect 272 bytes, however the total size of ESR is only 107 bytes
            byteBuf.writeBytes(new byte[233]);
        }
        channel.writeAndFlush(byteBuf).addListener(future -> {
            if (future.isSuccess()) {
                log.debug(s + " has sent to " + channel.remoteAddress());
            } else {
                log.error(s + "sent to " + channel.remoteAddress() + " failed", future.cause());
            }
        });
        //channel.pipeline().remove("shit");

    }


    /**
     * @param httpMessage as it's name
     * @return true if it has
     */

    public static boolean checkConnectHeader(String httpMessage) {
        return (httpMessage.startsWith("Connect") || httpMessage.startsWith("CONNECT") || httpMessage.startsWith("connect"));
    }

    public static boolean checkRawHttpResponse(String httpMessage) {
        return (httpMessage.startsWith("Http") || httpMessage.startsWith("HTTP") || httpMessage.startsWith("http"));
    }

    public static boolean checkConnEstablishedResponse(String msg) {
        return msg.startsWith(Constants.CONNECTION_ESTABLISHED.toString().substring(0, 32));
    }


    public static String getHttpMessageFromByteBuf(Object msg) {
        byte[] bytes = null;
        ByteBuf byteBuf = (ByteBuf) msg;
        if (ControlCentre.auth && !ControlCentre.client) {
            bytes = new byte[((ByteBuf) msg).readableBytes()];
            byteBuf.getBytes(8, bytes);
        } else {
            bytes = new byte[((ByteBuf) msg).readableBytes()];
            byteBuf.getBytes(0, bytes);
        }
        return new String(bytes);
    }

    public static String getStringFromByteBuf(Object msg) {
        byte[] bytes = new byte[((ByteBuf) msg).readableBytes()];
        ((ByteBuf) msg).getBytes(0, bytes);
        return new String(bytes);
    }


    /*
    public static byte[] getSpecificBytesFromByteBuf(ByteBuf byteBuf) {

    }*/


    public static ByteBuf addIdAndLength(ByteBuf byteBuf, byte[] id, int length) {
        byte[] bytes = getBytesFromByteBuf(byteBuf);
        ByteBuf newByteBuf = PooledByteBufAllocator.DEFAULT.buffer();
        newByteBuf.writeBytes(id);
        newByteBuf.writeInt(length);
        log.debug("Length field add: " + length);
        newByteBuf.writeBytes(bytes);
        return newByteBuf;
    }

    public static String getNameForConn(Channel channel) {
        return ProxyUtil.getLocalAddressAndPortFromChannel(channel) + ProxyUtil.getRemoteAddressAndPortFromChannel(channel);
    }

    public static String getNameForConn(ChannelHandlerContext context) {
        return getNameForConn(context.channel());
    }

    public static boolean checkIdMatched(byte[] s, byte[] d) {

        if (s.length != d.length) {
            return false;
        }
        for (int i = 0; i < s.length; i++) {
            if (s[i] != d[i]) return false;
        }
        return true;
    }

    public static boolean checkIdMatchedWithTail(byte[] s, byte[] d) {
        for (int i = s.length - 8; i < s.length; i++) {
            if (s[i] != d[8 - (s.length - i)]) return false;
        }
        return true;
    }


    public static ByteBuf[] splitChunkWith256Bytes(ByteBuf chunk) {
        int number = chunk.readableBytes() / 256 + (chunk.readableBytes() % 256 == 0 ? 0 : 1);
        ByteBuf byteBuf[] = new ByteBuf[number];
        int idx = 0;
        while (chunk.readableBytes() > 0) {
            int bufferSize = chunk.readableBytes() >= 256 ? 256 : chunk.readableBytes();
            byteBuf[idx] = Unpooled.wrappedBuffer(new byte[bufferSize]);
            chunk.readBytes(byteBuf[idx], 0, bufferSize);
            idx++;
        }
        log.debug("Fuck all these shit" + byteBuf.length);
        return byteBuf;
    }

    public static ByteBuf[] splitAndEnc(ByteBuf msg, byte[] id) throws Exception {
        ByteBuf byteBuf[] = splitChunkWith256Bytes(msg);
        for (int i = 0; i < byteBuf.length; i++) {
            byteBuf[i] = (ByteBuf) CryptoUtil.encrypt(byteBuf[i], id);
        }
        log.debug("The last one " + byteBuf[byteBuf.length - 1].readableBytes());
        return byteBuf;
    }

    public static ByteBuf removeTailId(ByteBuf msg) {
        /*
        byte[] src = new byte[8];
        msg.getBytes(msg.readerIndex() - 8, src);
        for (int i = 0; i < 8; i++) {
            if (src[i] != ControlCentre.id[i]) return msg;
        }*/
        return msg.readBytes(msg.readableBytes() - 8);
    }

    public static ByteBuf rmHeadId(ByteBuf msg) {
        ByteBuf byteBuf = Unpooled.buffer(msg.readableBytes() - 8);
        msg.getBytes(8, byteBuf);
        return byteBuf;
    }

    public static ByteBuf rmTailId(ByteBuf msg) {
        ByteBuf byteBuf = Unpooled.buffer(msg.readableBytes() - 8);
        msg.getBytes(0, byteBuf, msg.readableBytes() - 8);
        return byteBuf;
    }

    public static ByteBuf extractMsg(ByteBuf byteBuf, int start, int end) {
        ByteBuf msg = PooledByteBufAllocator.DEFAULT.buffer(end - start);
        byteBuf.getBytes(start, msg, 0, end - start);
        return msg;
    }

    public static ByteBuf rmZeroPadding(ByteBuf byteBuf) {
        int length = byteBuf.readableBytes();
        byte b = byteBuf.getByte(length - 1);
        while (b == 0) {
            length--;
            b = byteBuf.getByte(length - 1);
        }
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return ProxyUtil.getByteBufFromBytes(bytes);


    }

    public static void transmitToAnother(ByteBuf src, ByteBuf target) {
        byte[] bytes = getBytesFromByteBuf(src);
        target.retain(1);
        target.writeBytes(bytes);
    }

    public static void logErrorThenClose(Channel channel, Logger log, Throwable cause) {
        log.error("UnHandled Exception,channel to" + ProxyUtil.getRemoteAddressAndPortFromChannel(channel) + " will be closed", cause);
        channel.close();

    }

    public static void logErrorThenClose(ChannelHandlerContext ctx, Logger log, Throwable cause) {
        logErrorThenClose(ctx.channel(), log, cause);

    }

    public static void logInActiveThenClose(Channel channel, Logger log) {
        log.info("Channel to " + ProxyUtil.getRemoteAddressAndPortFromChannel(channel) + "inactive will be closed");
        channel.close();
    }



}
