package base.arch;

import io.netty.bootstrap.ServerBootstrap;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * @date 2020/1/29
 */
@Slf4j
public abstract class AbstractProxy {

    private static final String LOCALHOST = "127.0.0.1";
    private ServerBootstrap server = new ServerBootstrap();
    private int bindPort;
    private String serverAddress = LOCALHOST;
    private int threadNumber = Platform.processorsNumber * 2 + 1;
    private final ChannelGroup allChannels = new ChannelGroup();
    public static boolean CLIENT_MODE = true;


    public AbstractProxy() {
    }

    public AbstractProxy(Config config) {
        if (config.getServerAddress() != null) {
            this.serverAddress = config.getServerAddress();
        }
        this.bindPort = config.getLocalPort();
    }


    {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                allChannels.closeChannels();
            }
        });
    }

    public abstract void start();


}