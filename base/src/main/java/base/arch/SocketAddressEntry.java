package base.arch;

import base.constants.Packets;
import base.interfaces.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

/**
 * @author kikyou
 * Created at 2020/2/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketAddressEntry {
    private String host;
    private Short port;

    @Override
    public String toString() {
        return host + ":" + port;
    }

    public static SocketAddressEntry getFromEncryptedBuf(ByteBuf buf, Crypto crypto) {
        buf = crypto.decrypt(buf);
        String host = buf.readCharSequence(buf.readableBytes() - Packets.FILED_PORT_LENGTH, StandardCharsets.US_ASCII).toString();
        short port = buf.readShort();
        return new SocketAddressEntry(host, port);
    }

    public ByteBuf encryptEntry(Crypto crypto, ByteBufAllocator alloc) {
        byte[] hostBytes = host.getBytes(StandardCharsets.US_ASCII);
        ByteBuf buf = alloc.buffer(hostBytes.length + Packets.FILED_PORT_LENGTH + Packets.FILED_HOST_LENGTH);
        buf.writeBytes(hostBytes);
        buf.writeShort(port);
        buf = crypto.encrypt(buf);
        return buf;
    }

}
