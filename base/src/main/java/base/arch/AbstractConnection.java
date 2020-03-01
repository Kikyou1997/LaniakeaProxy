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
public abstract class AbstractConnection extends ChannelInboundHandlerAdapter {

    protected Channel channel;
    protected ChannelHandlerContext ctx;
    protected static int CONNECT_TIME_OUT = 2000;

    protected AbstractConnection c2PConnection;
    protected AbstractConnection p2SConnection;
    protected static NioEventLoopGroup group = new NioEventLoopGroup(Platform.processorsNumber * 2);
    protected int id;

    public AbstractConnection() {
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        this.channel = ctx.channel();
        super.channelRegistered(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.channel = ctx.channel();
        this.ctx = ctx;
        doRead(ctx, (ByteBuf) msg);
        ((ByteBuf) msg).release(((ByteBuf) msg).refCnt());
        ReferenceCountUtil.safeRelease(msg);
        ctx.fireChannelRead(msg);
        ReferenceCountUtil.safeRelease(msg);
    }

    protected abstract void doRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception;

    public ChannelFuture writeData(ByteBuf buf) {
        int count = 0;
        while (!channel.isWritable() && count < 32) {
            LockSupport.parkNanos(5000);
            count++;
        }
        ChannelFuture future = channel.writeAndFlush(buf).syncUninterruptibly();
        try {
            int refCnt = buf.refCnt();
            ReferenceCountUtil.release(buf, refCnt);
        } catch (IllegalReferenceCountException e) {
            log.info("Recycle buf failed", e);
        } finally {
            return future;
        }
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
