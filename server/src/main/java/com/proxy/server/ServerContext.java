package com.proxy.server;

import base.arch.Session;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author kikyou
 * Created at 2020/2/20
 */
public class ServerContext {


    private static Map<Integer/*id*/, Session> sessionMap = new ConcurrentHashMap<>();
    private static Map<String/*username*/, Integer/*id*/> usernameIdMap = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);

    static {
        scheduled.scheduleAtFixedRate(new DeleteExpiredSessionService(), 5, 10, TimeUnit.MINUTES);
    }


    public static Session createSession(int id, String username, byte[] iv) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(iv);
        Session session = sessionMap.get(id);
        if (session == null) {
            session = new Session(System.currentTimeMillis(), username, iv);
            sessionMap.put(id, session);
            Integer expiredId = usernameIdMap.get(username);
            if (expiredId != null) {
                updateExpiredSessionInfo(expiredId, id);
            }
            usernameIdMap.put(username, id);
        }
        return session;
    }

    public static Session getSession(int id) {
        return sessionMap.get(id);
    }

    public static void updateSessionLastActiveTime(int id) {
        var session = getSession(id);
        if (session != null) {
            session.setLastActiveTime(System.currentTimeMillis());
        }
    }

    private static void updateExpiredSessionInfo(int expiredId, int newId) {
        Session expiredSessionInfo = sessionMap.get(expiredId);
        if (expiredSessionInfo != null) {
            Session updatedSessionInfo = sessionMap.get(newId);
            expiredSessionInfo.setIv(updatedSessionInfo.getIv());
        }
    }

    private static class DeleteExpiredSessionService extends Thread {

        @Override
        public void run() {
            for (Integer id : sessionMap.keySet()) {
                Session s = sessionMap.get(id);
                if (s.isExpired()) {
                    sessionMap.remove(id);
                }
            }
        }
    }

}
