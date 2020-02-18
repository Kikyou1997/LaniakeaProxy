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

    protected AbstractConnection c2PConnection;
    protected AbstractConnection p2SConnection;
    protected Queue<ConnectionStep> steps = new LinkedList<>();
    protected ChannelHandlerContext context;

    public AbstractConnectionStream(AbstractConnection c2PConnection, ChannelHandlerContext context) {
        this.c2PConnection = c2PConnection;
        this.context = context;
        initStream();
    }

    public AbstractConnectionStream addStep(ConnectionStep step) {
        steps.offer(step);
        return this;
    }

    protected abstract void initStream();

    public static abstract class ConnectionStep implements Handler<Object> {
        protected Object lastResult = null;

        public void setLastResult(Object lastResult) {
            this.lastResult = lastResult;
        }
    }

    public AbstractConnectionStream then(ByteBuf buf) throws Exception{
        ConnectionStep step = steps.poll();
        step.handle(buf, context);
        return this;
    }

    public AbstractConnectionStream handle(ByteBuf buf) throws Exception{
        ConnectionStep step = steps.peek();
        step.handle(buf, context);
        return this;
    }

    public ConnectionStep nextStep() {
        return steps.poll();
    }

    public ConnectionStep peek() {
        return steps.peek();
    }

    public void close() {
        p2SConnection.disconnect();
        c2PConnection.disconnect();
    }

}