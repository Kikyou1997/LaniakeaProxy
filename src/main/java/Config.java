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

    private Integer bindPort;
    private String serverAddress;
    private String encryptionMethod;
    private String username;
    private String password;
    private Boolean statistic;
}