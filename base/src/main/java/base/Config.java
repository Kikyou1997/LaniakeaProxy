package base;

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

    public static Config config = null;
    public static transient String CLIENT_CONFIG_FILE_PATH = "/etc/andromeda/config.json";
    public static transient String SERVER_CONFIG_FILE_PATH = "/etc/andromeda/server_config.json";
    private static transient HashMap<String/*用户名*/, User/*用户信息*/> userInfoMap = new HashMap<>();

    private String bindAddress;
    private Integer bindPort;
    private String serverAddress;
    private int serverPort;
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
        private transient byte[] secretKeyBin;

        public byte[] getSecretKeyBin() {
            if (secretKeyBin == null) {
                secretKeyBin = CryptoUtil.decodeFromString(secretKey);
            }
            return secretKeyBin;
        }
    }

    static {
        // loadConfig File
        if (AbstractProxy.CLIENT_MODE) {
            config = IOUtil.getSettings(CLIENT_CONFIG_FILE_PATH);
        } else {
            config = IOUtil.getSettings(SERVER_CONFIG_FILE_PATH);
        }
    }

    {
        initUserInfoMap();
    }

    private void initUserInfoMap() {
        for (User u : users) {
            userInfoMap.put(u.username, u);
        }
    }

    public static User getUserInfo(String username) {
        return userInfoMap.get(username);
    }

    public static byte[] getUserSecretKeyBin(String username) {
        User u = getUserInfo(username);
        if (u != null) {
            return u.getSecretKeyBin();
        }
        return null;
    }
}