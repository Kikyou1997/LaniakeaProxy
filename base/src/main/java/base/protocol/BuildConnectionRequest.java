package base.protocol;

import base.arch.SocketAddressEntry;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.StandardCharsets;

public class BuildConnectionRequest extends LaniakeaPacket {

    private SocketAddressEntry socketAddressEntry;

    public BuildConnectionRequest(byte code, int id, int length, SocketAddressEntry socketAddressEntry) {
        super(code, id, length);
        this.socketAddressEntry = socketAddressEntry;
    }


    public ByteBuf toByteBuf(ByteBufAllocator alloc, Crypto crypto) {
        ByteBuf content = alloc.buffer()
                .writeBytes(socketAddressEntry.getHost().getBytes(StandardCharsets.US_ASCII))
                .writeShort(socketAddressEntry.getPort());
        content = crypto.encrypt(content);
        super.setContent(content);
        return super.toByteBuf(alloc);
    }

}
