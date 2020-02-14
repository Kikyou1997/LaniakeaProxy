import io.netty.channel.Channel;

/**
 * @author kikyou
 * Created at 2020/2/14
 */
public class ConnectionStream extends  AbstractConnectionStream{

    @Override
    public AbstractConnectionStream newStream(Channel connectionFromClient, String ip, int port) {
        String host = ProxyUtil.getRemoteAddressAndPortFromChannel(connectionFromClient);

    }


}
