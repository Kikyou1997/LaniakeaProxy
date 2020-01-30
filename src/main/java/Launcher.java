import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.proxy.Socks5ProxyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;


/**
 * @author kikyou
 * @date 2020/1/29
 */
@Slf4j
public class Launcher {

    private static final String AUTH = "a";
    private static final String PORT = "p";
    private static final String SERVER_IP = "S";
    private static final String SERVER_PORT = "P";
    private static final String CONFIG = "c";
    private static final String STATISTIC = "s";

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;
        options.addOption(null, AUTH, false, "Run with authentication");
        options.addOption(null, CONFIG, true, "Run with specific configuration file");
        options.addOption(null, PORT, true, "Listen on specific port");
        options.addOption(null, SERVER_PORT, true, "Specify the port of remote proxy server");
        options.addOption(null, SERVER_IP, true, "Bind on specific address");
        options.addOption(null, STATISTIC, false, "enable statistic");
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            log.error("parse command failed", e);
        }


    }
}