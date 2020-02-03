import abstracts.Connection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public class Proxy2ServerConnection extends Connection {

    private String ip;
    private int port;

    public Proxy2ServerConnection(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, Object msg) {

    }

    @Override
    protected void disconnect() {
        super.remoteServer.close();
    }
}
