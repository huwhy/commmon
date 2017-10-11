package cn.huwhy.common.concurrent.locks;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ReentrantKeyLock<T extends Serializable> {

    private final Set<T>             keySet = new HashSet<>();
    private final HashMap<T, Thread> map    = new HashMap<>();

    public void lock(T key) {
        boolean ok = false;
        while (!ok) {
            synchronized (keySet) {
                ok = keySet.add(key);
                if (ok) {
                    map.put(key, Thread.currentThread());
                } else if (Thread.currentThread() == map.get(key)) {
                    ok = true;
                } else {
                    try {
                        keySet.wait();
                    } catch (InterruptedException ignore) {}
                }
            }
        }
    }

    public void unlock(T key) {
        synchronized (keySet) {
            if (keySet.remove(key)) {
                map.remove(key);
            }
            keySet.notifyAll();
        }
    }

    public static void main(String[] args) {
        final ReentrantKeyLock<Integer> keyLock = new ReentrantKeyLock<>();
        final Integer key = 1;
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + ": start");
                keyLock.lock(key);
                try {
                    try {
                        System.out.println(Thread.currentThread().getName() + ": get lock");
                        Thread.sleep(550l);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } finally {
                    keyLock.unlock(key);
                }
                System.out.println(Thread.currentThread().getName() + ": end");
            }, "Thread-" + (i + 1)).start();
        }
    }
}
