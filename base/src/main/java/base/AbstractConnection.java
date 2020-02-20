package base;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * @date 2020/1/29
 */
@Slf4j
public abstract class AbstractConnection extends SimpleChannelInboundHandler<ByteBuf> {

    protected Channel channel;
    private static int THREAD_NUMBER = 1;
    // UNIT:millisecond
    protected static int CONNECT_TIME_OUT = 2000;
    protected EventLoopGroup eventLoops = new NioEventLoopGroup(THREAD_NUMBER);

    protected AbstractConnection c2PConnection;
    protected AbstractConnection p2SConnection;
    protected int id;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        channel = ctx.channel();
        doRead(ctx, msg);
    }

    protected abstract void doRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception;

    public abstract ChannelFuture writeData(ByteBuf data);

    protected void disconnect() {
        p2SConnection.disconnect();
        c2PConnection.disconnect();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel {} is inactive,  will be closed", ProxyUtil.getRemoteAddressAndPortFromChannel(ctx));
        disconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        disconnect();
    }

    public boolean isChannelActive() {
        return channel.isActive();
    }
}
