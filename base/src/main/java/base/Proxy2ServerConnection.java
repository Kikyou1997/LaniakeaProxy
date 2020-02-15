package base;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public class Proxy2ServerConnection extends AbstractConnection {

    private String ip;
    private int port;
    private Channel channel;

    public Proxy2ServerConnection(String ip, int port) {
        this.ip = ip;
        this.port = port;
        buildConnection2Remote(ip, port);
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, Object msg) {

    }


    @Override
    protected ChannelFuture writeData(ByteBuf data) {
        return remoteServer.writeAndFlush(data);
    }

    @Override
    protected boolean buildConnection2Remote(String ip, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(super.eventLoops);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIME_OUT);
        ChannelFuture future = bootstrap.connect(ip, port);
        future.syncUninterruptibly();
        this.channel = future.channel();
        return future.isSuccess();
    }

    @Override
    protected void disconnect() {
        super.remoteServer.close();
    }


}
