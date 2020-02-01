import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * @author kikyou
 * @date 2020/1/29
 */
@Data
@AllArgsConstructor
public class Config implements Serializable {

    public static transient String CLIENT_CONFIG_FILE_PATH = "/etc/andromeda/config.json";
    public static transient String SERVER_CONFIG_FILE_PATH = "/etc/andromeda/server_config.json";
    private transient HashMap<String/*用户名*/, User/*用户信息*/> userInfoMap = new HashMap<>();

    private Integer bindPort;
    private String serverAddress;
    private String encryptionMethod;
    private Boolean statistic;
    // 客户端配置
    private String username;
    private String secretKey;
    //服务端配置
    private List<User> users;

    @Data
    @AllArgsConstructor
    public static class User {
        private String username;
        /*BASE64编码*/
        private String secretKey;
        private Long usedTraffics;
    }

}