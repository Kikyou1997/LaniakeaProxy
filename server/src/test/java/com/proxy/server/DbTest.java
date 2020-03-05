package com.proxy.server;


import org.junit.BeforeClass;
import org.junit.Test;

public class DbTest {

    @BeforeClass
    public static void init() {
        Db.initDb();
    }

    @Test
    public void recordTrack() throws Exception {
        int count = 0;
        long lastTime = System.currentTimeMillis();
        while (count < 60) {
            Db.Track track = new Db.Track("kikyou", lastTime, 60, "127.0.0.1");
            Thread.sleep(500);
            Db.recordTrack(track);
            lastTime = System.currentTimeMillis();
            count++;
        }
    }

}
