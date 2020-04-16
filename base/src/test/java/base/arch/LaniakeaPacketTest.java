package base.arch;

import base.constants.RequestCode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;

import static org.junit.Assert.*;

public class LaniakeaPacketTest {

    @Test
    public void toByteBuf() {
        ByteBuf buf = new LaniakeaPacket(RequestCode.AUTH_REQ, 0, 0, null).toByteBuf(UnpooledByteBufAllocator.DEFAULT);
        assertEquals(buf.readableBytes() , 9);
    }
}