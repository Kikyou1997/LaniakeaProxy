package base;

import lombok.extern.slf4j.Slf4j;


/**
 * @author kikyou
 * @date 2020/1/29
 */
@Slf4j
public class Launcher {

    public static void main(String[] args) throws Exception {

        String clientMode = "-C";
        String serverMode = "-S";
        if (args[0].equals(clientMode)) {
            AbstractProxy.CLIENT_MODE = true;
        } else {
            AbstractProxy.CLIENT_MODE = false;
        }
        AbstractProxy proxy = new AbstractProxy(Config.config);
        proxy.start();
    }
}