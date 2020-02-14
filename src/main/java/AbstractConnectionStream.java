import abstracts.AbstractConnection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kikyou
 * Created at 2020/1/29
 */
public abstract class AbstractConnectionStream {

    private Map<String, AbstractConnection> host2TcpConnectionMap = new ConcurrentHashMap<>();

    private Client2ProxyConnection client2ProxyConnection = null;

    public abstract AbstractConnectionStream newStream(Channel connectionFromClient, String ip, int port);


}
