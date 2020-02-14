package abstracts;

import constants.Packets;
import interfaces.Handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kikyou
 * @date 2020/2/1
 */
public abstract class AbstractHandler<R> implements Handler<R> {

    protected static final int retryTimes = 3;

    protected ChannelHandlerContext context;

    protected static Map<Integer/*用户id*/, Long/*添加时间*/> idTimeMap = new ConcurrentHashMap<>();

    protected static Map<Integer/*用户id*/, String/*用户名*/> idNameMap = new ConcurrentHashMap<>();

    protected static Map<Integer/*用户id*/, byte[]/*iv*/> idIvMap = new ConcurrentHashMap<>();

    protected static final int REQ_CODE_POS = Packets.MAGIC_LENGTH;


    protected void sendResponse(ByteBuf response) throws InterruptedException{
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

    protected int getId(ByteBuf buf){
        return -1;
    }

    public static void main(String[] args) throws Exception {

    }

}