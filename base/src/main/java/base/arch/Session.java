package base.arch;

import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public class Session {
    long lastActiveTime;
    String username;
    byte[] iv;
    private long expiredTime = TimeUnit.HOURS.toMillis(2);

    public Session(long lastActiveTime, String username, byte[] iv) {
        this.lastActiveTime = lastActiveTime;
        this.username = username;
        this.iv = iv;
        }

    public boolean isExpired() {
        return System.currentTimeMillis() - lastActiveTime > expiredTime;
    }
}