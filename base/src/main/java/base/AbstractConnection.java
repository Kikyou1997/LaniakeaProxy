package base;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public abstract class AbstractConnection extends SimpleChannelInboundHandler<Object> {

    protected Channel remoteServer;
    private static int THREAD_NUMBER = 1;
    // UNIT:millisecond
    protected static int CONNECT_TIME_OUT = 2000;
    protected EventLoopGroup eventLoops = new NioEventLoopGroup(THREAD_NUMBER);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        doRead(ctx, msg);
    }

    /*
     由于RequestProcessor 已经处理了认证请求和解密问题 这里得到的就是原文
     该方法
     成功返回true
     */
    protected abstract boolean buildConnection2Remote(String ip, int port);

    protected abstract void doRead(ChannelHandlerContext ctx, Object msg);


    protected abstract ChannelFuture writeData(ByteBuf data);

    abstract protected void disconnect();

    protected static class Client2ProxyChannelInitializer extends ChannelInitializer<NioSocketChannel>{

        private int id;

        public Client2ProxyChannelInitializer(int id) {
            this.id = id;
        }

        @Override
        protected void initChannel(NioSocketChannel ch) throws Exception {
            ch.pipeline().
                    addLast(new HeadersPrepender.RequestHeadersPrepender(id))
        }
    }

    protected static class Proxy2ServerChannelInitializer extends ChannelInitializer<NioSocketChannel> {

        private int id;

        public Proxy2ServerChannelInitializer(int id) {
            this.id = id;
        }

        @Override
        protected void initChannel(NioSocketChannel ch) throws Exception {
            ch.pipeline().addLast(new HeadersPrepender.ResponseHeadersPrepender(id));
        }
    }
}
