import abstracts.Connection;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kikyou
 * Created at 2020/1/29
 */
public class ConnectionStream {

    private Map<String, Connection> host2TcpConnectionMap = new ConcurrentHashMap<>();

    private Client2ProxyConnection client2ProxyConnection = null;

    public static ConnectionStream newStream(Channel connectionFromClient, String ip, int port) {

    }

}
