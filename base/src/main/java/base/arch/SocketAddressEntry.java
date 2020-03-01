package base.arch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kikyou
 * Created at 2020/2/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketAddressEntry {
    private String host;
    private short port;

    @Override
    public String toString() {
        return host + ":" + port;
    }

}
