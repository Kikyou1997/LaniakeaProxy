package base;

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





    public static boolean checkRawHttpResponse(String httpMessage) {
        return (httpMessage.startsWith("Http") || httpMessage.startsWith("HTTP") || httpMessage.startsWith("http"));
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

    public static String constructSocketAddressString(String ip, int port) {
        return ip + ":" + port;
    }


}
