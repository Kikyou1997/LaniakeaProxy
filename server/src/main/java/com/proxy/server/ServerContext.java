package com.proxy.server;

import base.interfaces.Crypto;
import base.interfaces.Listener;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author kikyou
 * Created at 2020/2/20
 */
public class ServerContext {


    private static Map<Integer/*id*/, Session> sessionMap = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);

    static {
        scheduled.scheduleAtFixedRate(new DeleteExpiredSessionService(), 5, 10, TimeUnit.MINUTES);
    }

    @Data
    public static class Session {
        long lastActiveTime;
        String username;
        byte[] iv;
    }

    public static Session getSession(int id) {
        Session session = sessionMap.get(id);
        if (session == null) {
            session = new Session();
            session.setLastActiveTime(System.currentTimeMillis());
            sessionMap.put(id, session);
        }
        return session;
    }

    private static class DeleteExpiredSessionService extends Thread{

        private long expiredTime = TimeUnit.HOURS.toMillis(2);

        @Override
        public void run() {
            for (Integer id : sessionMap.keySet()) {
                Session s = sessionMap.get(id);
                if (System.currentTimeMillis() - s.getLastActiveTime() >= expiredTime){
                    sessionMap.remove(id);
                }
            }
        }
    }

}
