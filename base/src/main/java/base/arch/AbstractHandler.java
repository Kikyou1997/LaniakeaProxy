package base.arch;

import base.constants.Packets;
import base.interfaces.Handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kikyou
 * @date 2020/2/1
 */
public abstract class AbstractHandler<R> extends SimpleChannelInboundHandler<ByteBuf> implements Handler<R> {


    protected static final int retryTimes = 3;

    protected ChannelHandlerContext context;

    protected void sendResponse(ByteBuf response) {
        boolean succeed = false;
        int count = 0;
        while (!succeed && count < retryTimes) {
            try {
                ChannelFuture future = context.writeAndFlush(response).sync();
                succeed = future.isSuccess();
            } catch (Exception e) {
                e.printStackTrace();
            }
            count++;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        this.context = ctx;
        handle(msg, ctx);
    }
}