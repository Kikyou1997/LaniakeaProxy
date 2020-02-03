package abstracts;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public abstract class Connection extends SimpleChannelInboundHandler<Object> {

    protected Channel remoteServer;
    protected boolean tunnel;

    protected void buildConnection2Remote(Bootstrap bootstrap, String ip, int port,  Channel channel) {

    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        doRead(ctx, msg);
    }

    protected abstract void doRead(ChannelHandlerContext ctx, Object msg);

    protected Future write(Object msg) {
        return remoteServer.writeAndFlush(msg);
    }

    abstract protected void disconnect();

}
