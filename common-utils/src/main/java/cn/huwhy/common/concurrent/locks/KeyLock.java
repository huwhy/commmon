package cn.huwhy.common.concurrent.locks;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class KeyLock<T extends Serializable> {
    private final Set<String> keySet = new HashSet<>();
    private final String prefix;

    public KeyLock(String prefix) {
        this.prefix = prefix;
    }

    public boolean tryLock(T key) {
        synchronized (keySet) {
            return keySet.add(prefix + key);
        }
    }

    public void lock(T key) {
        synchronized (keySet) {
            while (!tryLock(key)) {
                try {
                    keySet.wait();
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    public void unlock(T key) {
        synchronized (keySet) {
            if (keySet.contains(prefix + key)) {
                keySet.remove(prefix + key);
            }
            keySet.notifyAll();
        }
    }
}
