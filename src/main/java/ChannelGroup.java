import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author kikyou
 * Created at 2020/1/30
 */
public class ChannelGroup {

    private ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();
    private volatile boolean closed = false;

    public void registerChannel(Channel channel) {
        if (!closed){
            channels.putIfAbsent(ProxyUtil.getLocalAddressAndPortFromChannel(channel), channel);
        }
    }

    public void deregisterChannel(Channel channel) {
        if (!closed) {
            channels.remove(ProxyUtil.getLocalAddressAndPortFromChannel(channel));
        }
    }

    public void closeChannels() {
        closed = true;
        for (String k : channels.keySet()) {
            Channel c = channels.get(k);
            c.close();
        }
        channels.clear();
    }

}
