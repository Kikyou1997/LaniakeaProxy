import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public class ConnectionStream {

    private Map<String, Proxy2ServerConnection> host2TcpConnectionMap = new ConcurrentHashMap<String, Proxy2ServerConnection>();
    private Client2ProxyConnection client2ProxyConnection = null;

    private void send2Remote() {

    }

    private void initunnel() {

    }

    private void sned2Client() {

    }
}
