package base;

import base.interfaces.Handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author kikyou
 * Created 2020/2/16
 */
public abstract class AbstractConnectionStream {

    protected Client2ProxyConnection c2PConnection;
    protected Proxy2ServerConnection p2SConnection;
    protected Queue<ConnectionStep> steps = new LinkedList<>();

    public AbstractConnectionStream(Client2ProxyConnection c2PConnection) {
        this.c2PConnection = c2PConnection;
        initStream();
    }

    public AbstractConnectionStream addStep(ConnectionStep step) {
        steps.offer(step);
        return this;
    }

    protected abstract void initStream();

    protected static abstract class ConnectionStep implements Handler<Object> {
        protected Object lastResult = null;

        public void setLastResult(Object lastResult) {
            this.lastResult = lastResult;
        }
    }

    protected void then(ByteBuf buf, ChannelHandlerContext context) throws Exception{
        ConnectionStep step = steps.poll();
        step.handle(buf, context);
    }

}