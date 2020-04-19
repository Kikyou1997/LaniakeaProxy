package base.arch;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class LockUtil {

    public static <T> boolean waitUntilTrue(Predicate<T> condition, T target, int retryTimes, int waitInterval) {
        while (!condition.test(target) && retryTimes-- > 0) {
            try {
                Thread.sleep(waitInterval);
            } catch (InterruptedException e){
                // do nothing
            }
        }
        return condition.test(target);
    }
}
