package com.proxy.server;

import base.arch.*;
import base.constants.Packets;
import base.constants.ResponseCode;
import base.crypto.CryptoUtil;
import base.interfaces.Auth;
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
public class AuthImpl extends AbstractHandler<Void> implements Auth {

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
        super.context = ctx;
        if (msg instanceof ByteBuf) {
            if (isValid(msg)) {
                int id = idAllocator.incrementAndGet();
                ServerContext.Session session = ServerContext.getSession(id);
                session.setLastActiveTime(System.currentTimeMillis());
                ((ByteBuf) msg).readerIndex(USERNAME_POS - Packets.FIELD_IV_LENGTH);
                byte[] iv = new byte[Packets.FIELD_IV_LENGTH];
                ((ByteBuf) msg).readBytes(iv);
                session.setIv(iv);
                session.setUsername(getUsername((ByteBuf) msg));
                ByteBuf resp = createAuthResponse(id, ResponseCode.AUTH_RESP);
                sendResponse(resp);
            } else {
                ctx.writeAndFlush(MessageGenerator.generateDirectBuf(ResponseCode.AUTH_FAILED));
                context.channel().close();
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

    private ByteBuf createAuthResponse(int id, byte code) {
        return MessageGenerator.generateDirectBuf(code, Converter.convertInteger2ByteBigEnding(id));
    }


}
