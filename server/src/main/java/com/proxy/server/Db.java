package com.proxy.server;

import base.arch.Config;
import base.arch.Platform;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class Db {

    private static final String dbScheme = "jdbc:sqlite:";

    private static final String dbName = "statistics";

    /**
     * 配置的用户数一般不会很多，所以就凑或着吧
     */
    private static final String createConcreteUserTable = "CREATE TABLE IF NOT EXISTS  Track(" +
            "    username TEXT NOT NULL ," +
            "    startTime INTEGER DEFAULT 0," +
            "    endTime INTEGER DEFAULT 0," +
            "    usedTraffic INTEGER DEFAULT 0," +
            "    ip TEXT DEFAULT NULL" +
            ")";

    private static final BlockingQueue<Track> trackQueue = new LinkedTransferQueue<>();
    private static final Map<String/*用户名*/, BlockingQueue<Track>> toBeAggregated = new HashMap<>();

    private static final String insertIntoConcreteSql = "INSERT INTO Track VALUES (? ,? ,?, ?, ?)";

    private static final String selectUserUsedSql = "SELECT usedTraffic FROM Track WHERE username = ?  ORDER BY usedTraffic DESC LIMIT 1";

    private static PreparedStatement insertIntoConcreteStatement;


    private static Connection connection = null;

    public static void initDb() {
        new PersistenceConcreteTrackService().start();
        new ScheduledAggregateTrack().start();
        String url = Db.dbScheme + Platform.workDir + Platform.separator + dbName;
        try {
            Db.connection = DriverManager.getConnection(url);
            connection.createStatement().execute(createConcreteUserTable);
            for (Config.User u : Config.config.getUsers()) {
                long used = getUserUsedTrafficFromDb(u.getUsername());
                ServerContext.userTrafficMap.put(u.getUsername(), new AtomicLong(used));
            }
            insertIntoConcreteStatement = connection.prepareStatement(insertIntoConcreteSql);
        } catch (SQLException e) {
            log.error("Sql error", e);
            System.exit(-1);
        }
    }

    public static long getUserUsedTrafficFromDb(String username) throws SQLException {
        PreparedStatement p = Db.connection.prepareStatement(selectUserUsedSql);
        p.setString(1, username);
        return p.executeQuery().getLong(1);
    }

    public static void addRecord(Track track) {
        try {
            trackQueue.put(track);
        } catch (InterruptedException e) {
            // do nothing
            e.printStackTrace();
        }
    }


    public static void recordTrack(Track track) {
        try {
            BlockingQueue<Track> q = null;
            if ((q = toBeAggregated.get(track.getUsername())) == null) {
                q = new LinkedTransferQueue<>();
                toBeAggregated.put(track.getUsername(), q);
            }
            q.put(track);
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
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

    private static class PersistenceConcreteTrackService extends Thread {
        @Override
        public void run() {
            while (true) {
                Track track = null;
                try {
                    track = trackQueue.take();
                    insertIntoConcreteStatement.setString(1, track.username);
                    insertIntoConcreteStatement.setLong(2, track.getStartTime());
                    insertIntoConcreteStatement.setLong(3, track.getEndTime());
                    insertIntoConcreteStatement.setLong(4, track.getUsedTraffic());
                    insertIntoConcreteStatement.setString(5, track.getIp());
                    insertIntoConcreteStatement.execute();
                } catch (InterruptedException | SQLException e) {
                    log.warn("Unexpected interrupted", e);
                }
            }
        }
    }

    private static class ScheduledAggregateTrack extends Thread {
        // 可能会有一些问题
        private static class AggregateTrackService implements Runnable {
            @Override
            public void run() {
                for (String k : toBeAggregated.keySet()) {
                    BlockingQueue<Track> q = toBeAggregated.get(k);
                    if (q == null) {
                        continue;
                    }
                    try {
                        Track aggregated = new Track();
                        Track cur = q.take();
                        aggregated.setUsername(k);
                        aggregated.setIp(cur.getIp());
                        aggregated.setStartTime(cur.getRecordTime());
                        aggregated.setEndTime(cur.getRecordTime());
                        while (q.size() > 0) {
                            cur = q.take();
                            aggregated.setUsedTraffic(aggregated.getUsedTraffic() + cur.getUsedTraffic());
                            aggregated.setEndTime(Math.max(aggregated.getEndTime(), cur.getRecordTime()));
                            aggregated.setStartTime(Math.min(aggregated.getStartTime(), cur.getRecordTime()));
                        }
                        trackQueue.put(aggregated);
                    } catch (Exception e) {
                        // do nothing
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void run() {
            Executors.newScheduledThreadPool(2).scheduleAtFixedRate(new AggregateTrackService(), 0, 30, TimeUnit.SECONDS);
        }
    }

    @Data
    public static class Track {
        String username;
        long startTime;
        long endTime;
        long usedTraffic;
        String ip;
        transient long recordTime;

        public Track() {
        }

        public Track(String username, long recordTime, long usedTraffic, String ip) {
            this.username = username;
            this.usedTraffic = usedTraffic;
            this.ip = ip;
            this.recordTime = recordTime;
        }
    }

}
