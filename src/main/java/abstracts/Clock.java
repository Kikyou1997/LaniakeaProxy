package abstracts;

import java.util.concurrent.TimeUnit;

/**
 * @author kikyou
 * @date 2020/1/31
 */
public class Clock {

    //1024 * 64
    private static long currentTime = System.currentTimeMillis() >>> 16;

    static {
        new UpdateCurrentTime().start();
    }

    public static long getTime() {
        return currentTime;
    }

    private static class UpdateCurrentTime extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000 * 60);
                    Clock.currentTime = System.currentTimeMillis() >>> 16;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

    }
}