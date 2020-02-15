package base;

/**
 * @author kikyou
 * @date 2020/1/31
 */
public class Clock {

    //1024 * 64
    private static long currentTime = System.currentTimeMillis() >>> 16;
    private static byte[] currrentTimeBytesArrayRepresent = null;
    static {
        new UpdateCurrentTime().start();
    }

    public static long getTime() {
        return currentTime;
    }

    public static byte[] getTimeInBytes() {
        return currrentTimeBytesArrayRepresent;
    }

    private static class UpdateCurrentTime extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000 * 60);
                    Clock.currentTime = System.currentTimeMillis() >>> 16;
                    currrentTimeBytesArrayRepresent = getTimeInBytes();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public static void main(String[] args) throws Exception {

    }
}