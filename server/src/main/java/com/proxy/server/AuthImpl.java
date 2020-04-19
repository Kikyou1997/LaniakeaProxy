package com.proxy.server;

import base.arch.*;
import base.constants.Packets;
import base.constants.ResponseCode;
import base.crypto.CryptoUtil;
import base.interfaces.Auth;
import base.interfaces.Handler;
import base.protocol.LaniakeaPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kikyou
 * Created at 2020/1/31
 */
@Slf4j
public class AuthImpl implements Auth, Handler<Void> {

    private static AtomicInteger idAllocator = new AtomicInteger(0);
    private static final int HASH_POS = Packets.FIELD_CODE_LENGTH;
    private static final int USERNAME_POS = Packets.FIELD_CODE_LENGTH + Packets.FIELD_HASH_LENGTH + Packets.FIELD_IV_LENGTH;

    @Override
    public boolean isValid(Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        byte[] receivedHash = new byte[Auth.HASH_LENGTH];
        buf.readerIndex(HASH_POS);
        buf.readBytes(receivedHash);
        String username = getUsername(buf);
        Config.User user = Config.getUserInfo(username);
        if (user == null) {
            return false;
        }
        byte[] validHash = CryptoUtil.getSHA256Hash(user.getSecretKey(), Clock.getTimeInBytes());
        return Arrays.equals(receivedHash, validHash);
    }

    @Override
    public Void handle(Object msg, ChannelHandlerContext ctx) throws RuntimeException {
        if (msg instanceof ByteBuf) {
            if (isValid(msg)) {
                int id = idAllocator.incrementAndGet();
                byte[] iv = new byte[Packets.FIELD_IV_LENGTH];
                String username = null;

                ((ByteBuf) msg).readerIndex(USERNAME_POS - Packets.FIELD_IV_LENGTH);
                ((ByteBuf) msg).readBytes(iv);
                username = getUsername((ByteBuf) msg);
                ServerContext.createSession(id, username, iv);
                LaniakeaPacket resp = new LaniakeaPacket(ResponseCode.AUTH_RESP, id, 0, null);
                MessageUtil.sendSyncMsg(ctx, resp.toByteBuf(ctx.alloc()));
            } else {
                ctx.writeAndFlush(MessageUtil.generateDirectBuf(ResponseCode.AUTH_FAILED));
                ctx.close();
            }
        }
        return null;
    }

    private String getUsername(ByteBuf buf) {
        buf.readerIndex(USERNAME_POS);
        byte[] usernameBytes = new byte[buf.readableBytes()];
        buf.readBytes(usernameBytes);
        return new String(usernameBytes, StandardCharsets.US_ASCII);
    }


}
