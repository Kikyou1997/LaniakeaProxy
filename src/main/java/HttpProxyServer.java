import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kikyou
 * @date 2020/1/29
 */
@Slf4j
public class HttpProxyServer {


    private ServerBootstrap server = new ServerBootstrap();
    private int bindPort;
    private String serverAddress;
    private NioEventLoopGroup acceptThreads;
    private NioEventLoopGroup workerThreads;
    private int acceptThreadNumber = Math.min(Platform.processorsNumber, 2);
    private int workerThreadNumber = Platform.processorsNumber * 2 + 1;
    private final ChannelGroup allChannels = new ChannelGroup();

    public HttpProxyServer(ServerBootstrap server,
                           int bindPort,
                           String serverAddress,
                           NioEventLoopGroup acceptThreads,
                           NioEventLoopGroup workerThreads,
                           int acceptThreadNumber,
                           int workerThreadNumber) {
        this.server = server;
        this.bindPort = bindPort;
        this.serverAddress = serverAddress;
        this.acceptThreads = acceptThreads;
        this.workerThreads = workerThreads;
        this.acceptThreadNumber = acceptThreadNumber;
        this.workerThreadNumber = workerThreadNumber;
    }

    public HttpProxyServer(ServerBootstrap server,
                           int bindPort,
                           String serverAddress,
                           NioEventLoopGroup acceptThreads,
                           NioEventLoopGroup workerThreads
    ) {
        this.server = server;
        this.bindPort = bindPort;
        this.serverAddress = serverAddress;
        this.acceptThreads = acceptThreads;
        this.workerThreads = workerThreads;
    }

    {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                allChannels.closeChannels();
            }
        });
    }

    public void start(ChannelInitializer<NioServerSocketChannel> initializer) {
        server
                .group(new NioEventLoopGroup(acceptThreadNumber), new NioEventLoopGroup(workerThreadNumber))
                .childHandler(initializer)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .bind(serverAddress, bindPort)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        allChannels.registerChannel(future.channel());
                        log.info("Server booted");
                    }
                });
    }

    public static void main(String[] args) throws Exception {

    }
}