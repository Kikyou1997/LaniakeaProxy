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
        Db.recordTrack(1, 2, 3, 4);
        Db.recordTrack(1, 2, 3, 4);

    }

    @Test
    public void updateUserInfo() throws Exception {
        Db.updateUserInfo(new Db.UserInfo(1, 1, 1, "kikyou"));
    }

    @Test
    public void addNewUser() throws Exception{
        Db.addNewUser(new Db.UserInfo(1,1,1,"kikyou"));
    }
}
