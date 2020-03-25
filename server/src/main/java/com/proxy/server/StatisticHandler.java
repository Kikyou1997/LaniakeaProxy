package com.proxy.server;

import base.arch.LaniakeaPacket;
import base.arch.ProxyUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.traffic.TrafficCounter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class StatisticHandler extends ChannelInboundHandlerAdapter {

    private TrafficCounter trafficCounter = null;
    private String username = null;
    private String ip = null;

    public StatisticHandler(TrafficCounter trafficCounter) {
        this.trafficCounter = trafficCounter;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ip = ProxyUtil.getRemoteAddressAndPortFromChannel(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        if (username == null) {
            if (msg instanceof LaniakeaPacket) {
                LaniakeaPacket pkg = (LaniakeaPacket) msg;
                int id = pkg.getId();
                this.username = ServerContext.getSession(id).getUsername();
            }
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        try {
            long total = trafficCounter.cumulativeReadBytes() + trafficCounter.cumulativeWrittenBytes();
            log.debug("Connection for {} used traffic {}", ProxyUtil.getRemoteAddressAndPortFromChannel(ctx), total);
            AtomicLong usedTraffic = ServerContext.userTrafficMap.get(username);
            usedTraffic.compareAndSet(usedTraffic.get(), usedTraffic.get() + total);
            Db.recordTrack(new Db.Track(username, System.currentTimeMillis(), usedTraffic.get(), ip));
        } catch (Exception e){
            log.debug("Ignored exception,", e);
        }

    }
}
