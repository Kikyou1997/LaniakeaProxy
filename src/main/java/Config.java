import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author kikyou
 * @date 2020/1/29
 */
@Data
@AllArgsConstructor
public class Config implements Serializable {

    public static transient String CLIENT_CONFIG_FILE_PATH = "/etc/andromeda/config.json";
    public static transient String SERVER_CONFIG_FILE_PATH = "/etc/andromeda/server_config.json";

    private Integer bindPort;
    private String serverAddress;
    private String encryptionMethod;
    private String username;
    private String password;
    private Boolean statistic;
}