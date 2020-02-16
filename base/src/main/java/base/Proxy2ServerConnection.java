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

    private SocketAddressEntry socketAddress;
    private Channel channel;
    private AbstractConnectionStream connectionStream;
    public Proxy2ServerConnection(SocketAddressEntry entry, AbstractConnectionStream stream) throws Exceptions.ConnectionTimeoutException {
        this.socketAddress = entry;
        this.connectionStream = stream;
        if (!buildConnection2Remote(entry)) {
            throw new Exceptions.ConnectionTimeoutException(entry);
        }
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, ByteBuf msg) {

    }


    @Override
    public ChannelFuture writeData(ByteBuf data) {
        return remoteServer.writeAndFlush(data);
    }

    @Override
    protected boolean buildConnection2Remote(SocketAddressEntry socketAddress) {
        String ip = socketAddress.getHost();
        short port = socketAddress.getPort();
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
