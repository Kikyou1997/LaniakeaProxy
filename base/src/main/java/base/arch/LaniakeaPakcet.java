package base.arch;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 加密数据传输报文的格式
 */
@Data
@AllArgsConstructor
public class LaniakeaPakcet {

    private byte code;
    private int id;
    private int length;
    private ByteBuf content;

}
