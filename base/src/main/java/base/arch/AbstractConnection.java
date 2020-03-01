package base.arch;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.LockSupport;

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
    protected static NioEventLoopGroup group = new NioEventLoopGroup(Platform.processorsNumber * 2);
    protected int id;


    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.channel = ctx.channel();
        this.ctx = ctx;
        doRead(ctx, (V)msg);
        ReferenceCountUtil.safeRelease(msg);
        ctx.fireChannelRead(msg);
    }

    protected abstract void doRead(ChannelHandlerContext ctx, V msg) throws Exception;

    public ChannelFuture writeData(Object msg) {
        return channel.writeAndFlush(msg);
    }

    protected void disconnect() {
        if (p2SConnection != null) {
            closeConnection(p2SConnection);
        }
        if (c2PConnection != null) {
            closeConnection(c2PConnection);
        }
    }

    private void closeConnection(AbstractConnection connection) {
        Channel c = connection.channel;
        if (c != null) {
            c.writeAndFlush(new EmptyByteBuf(channel.alloc())).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    p2SConnection.channel.close();
                }
            });
        }

    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        disconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        disconnect();
    }

    public ChannelFuture buildConnection2Remote(SocketAddressEntry socketAddress) {
        return null;
    }

}
