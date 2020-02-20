package base;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * @date 2020/1/29
 */
@Slf4j
public abstract class AbstractConnection extends ChannelInboundHandlerAdapter {

    protected Channel channel;
    private static int THREAD_NUMBER = 1;
    // UNIT:millisecond
    protected static int CONNECT_TIME_OUT = 2000;
    protected EventLoopGroup eventLoops = new NioEventLoopGroup(THREAD_NUMBER);

    protected AbstractConnection c2PConnection;
    protected AbstractConnection p2SConnection;
    protected int id;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        channel = ctx.channel();
        doRead(ctx, (ByteBuf) msg);
    }

    protected abstract void doRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception;

    public abstract ChannelFuture writeData(ByteBuf data);

    protected void disconnect() {
        if (p2SConnection != null){
            p2SConnection.disconnect();
        }
        if (c2PConnection !=null){
            c2PConnection.disconnect();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel {} is inactive,  will be closed", ProxyUtil.getRemoteAddressAndPortFromChannel(ctx));
        disconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        disconnect();
    }

    public boolean isChannelActive() {
        return channel.isActive();
    }
}
