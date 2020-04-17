package base.protocol;

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
public class LaniakeaPacket {

    private byte code;
    private int id = -1;
    private int length;
    private ByteBuf content;

    public LaniakeaPacket() {
    }

    public LaniakeaPacket(byte code, int id, int length) {
        this.code = code;
        this.id = id;
        this.length = length;
    }

    public LaniakeaPacket(byte code, int id, int length, ByteBuf content) {
        this.code = code;
        this.id = id;
        this.length = length;
        this.content = content;
    }

    public ByteBuf toByteBuf(ByteBufAllocator alloc) {
        return alloc.buffer().writeByte(code)
                .writeInt(id)
                .writeInt(length)
                .writeBytes(content == null ? new EmptyByteBuf(alloc) : content);
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public ByteBuf getContent() {
        return content;
    }

    public void setContent(ByteBuf content) {
        this.content = content;
    }
}
