package com.proxy.server;

import base.arch.Platform;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

@Slf4j
public class Db {

    private static final String dbName = "statistics";

    /**
     * 配置的用户数一般不会很多，所以就凑或着吧
     */
    private static final String createUserInfoTable = "CREATE TABLE IF NOT EXISTS userInfo (" +
            "    id INTEGER  PRIMARY KEY AUTOINCREMENT," +
            "    userName TEXT DEFAULT NULL ," +
            "    usedTraffic INTEGER DEFAULT 0," +
            "    loginTimes INTEGER  DEFAULT 0" +
            ")";

    private static final String createConcreteUserTable = "CREATE TABLE IF NOT EXISTS  concrete(" +
            "    userId INTEGER NOT NULL ," +
            "    startTime INTEGER DEFAULT 0," +
            "    endTime INTEGER DEFAULT 0," +
            "    usedTraffic INTEGER DEFAULT 0" +
            ")";

    private static final BlockingQueue<ConcreteTrack> trackQueue = new LinkedTransferQueue<>();

    private static final String insertIntoConcreteSql = "INSERT INTO concrete VALUES (?, ? ,? ,?)";

    private static final String insertIntoUserInfoSql = "INSERT INTO userInfo(userName, usedTraffic, loginTimes) VALUES (? , ? , ?)";

    private static final String updateUserInfoSql = "UPDATE userinfo SET usedTraffic=?, loginTimes=? where userName=?";

    private static PreparedStatement insertIntoConcreteStatement;

    private static PreparedStatement insertIntoUserInfoStatement;

    private static PreparedStatement updateUserInfoStatement;

    private static Connection connection = null;

    public static void initDb() {
        String url = Platform.workDir + Platform.separator + dbName;
        try {
            Db.connection = DriverManager.getConnection(url);
            connection.createStatement().execute(createUserInfoTable);
            connection.createStatement().execute(createConcreteUserTable);
            insertIntoConcreteStatement = connection.prepareStatement(insertIntoConcreteSql);
            insertIntoUserInfoStatement = connection.prepareStatement(insertIntoUserInfoSql);
            updateUserInfoStatement = connection.prepareStatement(updateUserInfoSql);
        } catch (SQLException e) {
            log.error("Sql error", e);
            System.exit(-1);
        }
    }

    private static class PersistenceConcreteTrackService extends Thread {
        @Override
        public void run() {
            while (true) {
                ConcreteTrack track = null;
                try {
                    track = trackQueue.take();
                    recordTrack(track.userId, track.getStartTime(), track.getEndTime(), track.getUsedTraffic());
                } catch (InterruptedException e) {
                    log.warn("Unexpected interrupted", e);
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        //此id非彼id
        int id = 0;
        int usedTraffic = -1;
        int loginTimes = -1;
        String username;
    }

    @Data
    @AllArgsConstructor
    public static class ConcreteTrack {
        int userId;
        int startTime;
        int endTime;
        int usedTraffic;
    }

    public static void recordTrack(int uid, int start, int end, int used) {
        try {
            insertIntoConcreteStatement.setInt(1, uid);
            insertIntoConcreteStatement.setInt(2, start);
            insertIntoConcreteStatement.setInt(3, end);
            insertIntoConcreteStatement.setInt(4, used);
            insertIntoConcreteStatement.execute();
        } catch (SQLException e) {
            log.error("Record track failed", e);
        }
    }

    public static void updateUserInfo(UserInfo user) throws SQLException {
        if (user.getUsedTraffic() > 0) {
            updateUserInfoStatement.setInt(1, user.getUsedTraffic());
        }
        if (user.getLoginTimes() > 0) {
            updateUserInfoStatement.setInt(2, user.getLoginTimes());
        }
        if (user.getUsername() != null) {
            updateUserInfoStatement.setString(3, user.getUsername());
        }
        updateUserInfoStatement.execute();
    }

    /* https://stackoverflow.com/questions/2467125/reusing-a-preparedstatement-multiple-times 另一种更高效的方式 sample:
     * public void executeBatch(List<Entity> entities) throws SQLException {
     *     try (
     *         Connection connection = dataSource.getConnection();
     *         PreparedStatement statement = connection.prepareStatement(SQL);
     *     ) {
     *         int i = 0;
     *
     *         for (Entity entity : entities) {
     *             statement.setObject(1, entity.getSomeProperty());
     *             // ...
     *
     *             statement.addBatch();
     *             i++;
     *
     *             if (i % 1000 == 0 || i == entities.size()) {
     *                 statement.executeBatch(); // Execute every 1000 items.
     *             }
     *         }
     *     }
     * }
     */

}
