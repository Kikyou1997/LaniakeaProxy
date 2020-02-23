package base;

import base.ProxyUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author kikyou
 * Created at 2020/2/23
 */
@Slf4j
public class CustomizedIdleConnectionHandler extends IdleStateHandler {

    private static long READ_IDLE_TIME = 15;
    private static long WRITE_IDLE_TIME = 15;
    private static long ALL_IDLE_TIME = 30;


    public CustomizedIdleConnectionHandler() {
        super(READ_IDLE_TIME, WRITE_IDLE_TIME, ALL_IDLE_TIME, TimeUnit.SECONDS);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        log.debug("Channel of {} is idled will close", ProxyUtil.getRemoteAddressAndPortFromChannel(ctx));
        ctx.close();
    }
}
