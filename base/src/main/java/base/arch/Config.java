package base.arch;

import base.crypto.CFBCrypto;
import base.crypto.CryptoUtil;
import base.interfaces.Crypto;
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
    private Integer localPort;
    private String serverAddress;
    private int serverPort;
    private String encryptionMethod;
    private Boolean statistic;
    // 客户端配置
    private String username;
    // base64编码保存在本地
    private String secretKey;
    //服务端配置
    private List<User> users;
    private transient byte[] secretKeyBin = null;

    @Data
    @AllArgsConstructor
    public static class User {
        private String username;
        /*BASE64编码*/
        private String secretKey;
        private transient Long usedTraffics;
        private transient byte[] secretKeyBin;

        public byte[] getSecretKeyBin() {
            if (secretKeyBin == null) {
                secretKeyBin = CryptoUtil.base64Decode(secretKey);
            }
            return secretKeyBin;
        }
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
        } else {
            return config.getSecretKeyBin();
        }
    }

    public byte[] getSecretKeyBin() {
        if (this.secretKeyBin == null) {
            if (this.secretKey != null){
                this.secretKeyBin = CryptoUtil.base64Decode(this.secretKey);
            } else {
                return null;
            }
        }
        return secretKeyBin;
    }

    public static void loadSettings(boolean client) {
        if (!client) {
            config = IOUtil.getSettings(SERVER_CONFIG_FILE_PATH);
            config.initUserInfoMap();
            String encMethod = config.getEncryptionMethod();
            if (encMethod != null && !encMethod.equals(Crypto.GCM)) {
                if (encMethod.equals(Crypto.CFB)) {
                    CryptoUtil.setCrypto(new CFBCrypto(192, 16));
                }
            }
        } else {
            config = IOUtil.getSettings(CLIENT_CONFIG_FILE_PATH);
        }

    }
}