package cn.huwhy.common.util;

/**
 * @author huwhy
 * @date 2016/12/21
 * @Desc
 */
public class ThreadUtil {

    public static void sleep(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException("thread interrupted");
        }
    }

}
