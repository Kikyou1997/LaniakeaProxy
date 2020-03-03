package base.arch;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * @date 2020/1/29
 */
@Slf4j
public abstract class AbstractConnection<V> extends ChannelInboundHandlerAdapter {

    protected Channel channel;
    protected ChannelHandlerContext ctx;
    protected static int CONNECT_TIME_OUT = 2000;

    protected AbstractConnection c2PConnection;
    protected AbstractConnection p2SConnection;
    protected static NioEventLoopGroup group = new NioEventLoopGroup(Platform.coreNum * 2);
    protected int id;

    public AbstractConnection() {
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        this.channel = ctx.channel();
        super.channelRegistered(ctx);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        doRead(ctx, (V) msg);
        ctx.fireChannelRead(msg);
        ReferenceCountUtil.safeRelease(msg);
    }

    protected abstract void doRead(ChannelHandlerContext ctx, V msg) throws Exception;

    public ChannelFuture writeData(Object msg) {
        return channel.writeAndFlush(msg);
    }

    protected void disconnect() {
        if (p2SConnection != null && p2SConnection.channel != null){
            p2SConnection.channel.close();
        }
        if (c2PConnection != null && c2PConnection.channel != null) {
            c2PConnection.channel.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        disconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error", cause);
        disconnect();
    }

    public ChannelFuture buildConnection2Remote(SocketAddressEntry socketAddress) {
        return null;
    }

}
