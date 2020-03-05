package base.arch;

import java.io.File;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public class Platform {
    public static final int coreNum = Runtime.getRuntime().availableProcessors();
    public static String workDir = System.getProperty("user.dir");
    public static final String  separator = File.separator;
}

