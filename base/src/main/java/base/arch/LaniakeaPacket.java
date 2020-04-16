package base.arch;

import base.constants.RequestCode;
import base.constants.ResponseCode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 加密数据传输报文的格式
 */
@Data
@AllArgsConstructor
public class LaniakeaPacket {

    private byte code;
    private int id;
    private int length;
    private ByteBuf content;

    public LaniakeaPacket() {
    }

    public ByteBuf toByteBuf(ByteBufAllocator alloc) {
        return alloc.buffer().writeByte(code)
                .writeInt(id)
                .writeInt(length)
                .writeBytes(content == null ? new EmptyByteBuf(alloc) : content);
    }
}
