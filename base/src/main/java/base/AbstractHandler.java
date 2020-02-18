package base;

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

    protected int clientId = -1;

    protected static final int retryTimes = 3;

    protected ChannelHandlerContext context;

    protected static Map<Integer/*用户id*/, Long/*添加时间*/> idTimeMap = new ConcurrentHashMap<>();

    protected static Map<Integer/*用户id*/, String/*用户名*/> idNameMap = new ConcurrentHashMap<>();

    protected static Map<Integer/*用户id*/, byte[]/*iv*/> idIvMap = new ConcurrentHashMap<>();

    public AbstractHandler() {
    }

    /*
    * 该构造器方法用于客户端
    *
    */
    public AbstractHandler(int clientId, byte[] iv) {
        this.clientId = clientId;
        idNameMap.putIfAbsent(clientId, Config.config.getUsername());
        idIvMap.putIfAbsent(clientId, iv);
    }

    protected void sendResponse(ByteBuf response) throws InterruptedException {
        boolean succeed = false;
        int count = 0;
        while (!succeed && count < retryTimes) {
            ChannelFuture future = context.writeAndFlush(response).sync();
            succeed = future.isSuccess();
            count++;
        }
    }

    protected String getUsernameById(ByteBuf buf) {
        int id = getId(buf);
        return idNameMap.get(id);
    }

    // 其实际上 应该把它设置为抽象方法 在client/server module中分别实现其getId方法 但是我懒得改了 就先这样吧
    protected int getId(ByteBuf buf) {
        if (clientId != -1) {
            return clientId;
        } else {
            buf.readerIndex(Packets.FILED_CODE_LENGTH);
            return buf.readInt();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        handle(msg, ctx);
    }
}