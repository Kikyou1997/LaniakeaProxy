package abstracts;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.concurrent.Future;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public abstract class Connection<T extends HttpObject> extends SimpleChannelInboundHandler<Object> {

    private Channel remoteServer;
    private boolean tunnel;

    protected void buildConnection2Remote(Bootstrap bootstrap, String ip, int port,  Channel channel) {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }


    protected Future write(Object msg) {
        return remoteServer.writeAndFlush(msg);
    }

    abstract protected void disconnect();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

    }
}
