package base;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author kikyou
 * @date 2020/1/29
 */
@Slf4j
public abstract class AbstractConnection extends ChannelInboundHandlerAdapter {

    protected Channel channel;
    protected static int CONNECT_TIME_OUT = 2000;

    protected AbstractConnection c2PConnection;
    protected AbstractConnection p2SConnection;
    protected static NioEventLoopGroup group = new NioEventLoopGroup(Platform.processorsNumber * 2);
    protected int id;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        channel = ctx.channel();
        doRead(ctx, (ByteBuf) msg);
    }

    protected abstract void doRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception;

    public abstract ChannelFuture writeData(ByteBuf data);

    protected void disconnect() {
        if (p2SConnection != null) {
            if (p2SConnection.channel != null){
                p2SConnection.channel.close();
            }
        }
        if (c2PConnection != null) {
            if (c2PConnection.channel != null) {
                c2PConnection.channel.close();
            }
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        disconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    public ChannelFuture buildConnection2Remote(SocketAddressEntry socketAddress) {
        return null;
    }

}
