import abstracts.AbstractConnection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kikyou
 * Created at 2020/1/29
 */
public class Client2ProxyConnection extends AbstractConnection {

    private Map<String/*远程服务器的ip端口*/, Proxy2ServerConnection/*到远程服务器的连接*/> addressPort2ConnectionMap = new ConcurrentHashMap<>();

    private Channel client;

    private boolean tunnel;

    public Client2ProxyConnection(Channel channel, String ip, int port) {
        this.client = channel;
        String socketAddress = ProxyUtil.constructSocketAddressString(ip, port);
        addressPort2ConnectionMap.putIfAbsent(socketAddress, new Proxy2ServerConnection(ip, port));
    }

    @Override
    protected void doRead(ChannelHandlerContext ctx, Object msg) {

    }

    @Override
    protected void disconnect() {
        for (String s : addressPort2ConnectionMap.keySet()) {
            addressPort2ConnectionMap.get(s).disconnect();
        }
    }

    @Override
    protected ChannelFuture writeData(ByteBuf data) {
        return null;
    }
}
