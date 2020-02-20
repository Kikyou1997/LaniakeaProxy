package com.proxy.server;

import base.constants.Packets;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteOrder;

/**
 * @author kikyou
 * Created at 2020/2/18
 */
@Slf4j
public class CustomizedLengthBasedDecoder extends LengthFieldBasedFrameDecoder {

    public CustomizedLengthBasedDecoder() {
        super(ByteOrder.BIG_ENDIAN, Integer.MAX_VALUE, Packets.FIELD_CODE_LENGTH + Packets.FIELD_ID_LENGTH , 4, 0,0,false);
    }
}
