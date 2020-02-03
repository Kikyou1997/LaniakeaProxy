package interfaces;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author kikyou
 * Created at 2020/1/30
 */
public interface Handler<R> {

    R handle(Object msg) throws Exception;

    void setContext(ChannelHandlerContext context);

}
