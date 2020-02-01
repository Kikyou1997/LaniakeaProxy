package abstracts;

import interfaces.Handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kikyou
 * @date 2020/2/1
 */
public abstract class AbstractHandler<T, R> extends SimpleChannelInboundHandler<T> implements Handler<R> {

    protected ChannelHandlerContext context;

    protected static Map<Integer/*用户id*/, Long/*添加时间*/> idTimeMap = new ConcurrentHashMap<>();

    protected static Map<Integer/*用户id*/, String/*用户名*/> idNameMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        this.context = ctx;
        handle(msg);
    }

    public static void main(String[] args) throws Exception {

    }
}