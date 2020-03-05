package base.arch;

import io.netty.bootstrap.ServerBootstrap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

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
    private int threadNumber = Platform.coreNum * 2 + 1;
    private final ChannelGroup allChannels = new ChannelGroup();
    public static boolean CLIENT_MODE = true;
    // set configuration file path
    protected String configOption = "-c";

    public AbstractProxy() {
    }

    public AbstractProxy(Config config) {
        if (config.getServerAddress() != null) {
            this.serverAddress = config.getServerAddress();
        }
        this.bindPort = config.getLocalPort();
    }

    protected AbstractProxy prepare(String []args){return null;}

    public abstract void start();


}