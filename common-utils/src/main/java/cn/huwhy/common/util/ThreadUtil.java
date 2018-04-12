package cn.huwhy.common.util;

/**
 * @author huwhy
 * @date 2016/12/21
 * @Desc
 */
public class ThreadUtil {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException("thread interrupted");
        }
    }

    public static void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException("thread interrupted");
        }
    }

    public static void sleepMinute(int mins) {
        try {
            Thread.sleep(mins * 60 * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException("thread interrupted");
        }
    }

}
