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

    public static String getRemoteAddressAndPortFromChannel(Channel channel) {
        try {
            String host = ((InetSocketAddress) channel.remoteAddress()).getAddress().toString();
            int port = ((InetSocketAddress) channel.remoteAddress()).getPort();
            return host + ":" + port;
        } catch (Exception e) {
            log.error("Resolve socket address from channel failed", e);
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

    public static ByteBuf getByteBufFromBytes(byte[] bytes) {
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeBytes(bytes);
        return byteBuf;
    }


}
