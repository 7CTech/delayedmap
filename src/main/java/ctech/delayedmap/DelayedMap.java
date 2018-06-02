package ctech.delayedmap;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DelayedMap<K, V> {
    private class ObjectNotify {
        final Object obj = new Object();
        final AtomicBoolean notified = new AtomicBoolean(false);
    }

    private final HashMap<K, ObjectNotify> lockMap = new HashMap<>();
    private final HashMap<K, V> internalMap = new HashMap<>();

    public final DelayedMap<K, V> put(final K key, final V value)  {
        synchronized(lockMap) {
            internalMap.put(key, value);
            if (lockMap.get(key) != null) {
                synchronized(lockMap.get(key).obj) {
                    lockMap.get(key).obj.notify();
                    lockMap.get(key).notified.set(true);
                }
            }
        }
        return this;
    }

    public final V getAndWait(final K key, final long timeout) throws TimeoutException, InterruptedException {
        if (lockMap.get(key) == null) lockMap.put(key, new ObjectNotify());
        synchronized(lockMap.get(key).obj) {
            V res;
            do {
                res = internalMap.get(key);
                if (res == null) {
                    lockMap.get(key).obj.wait(timeout);
                    if (!lockMap.get(key).notified.get()) throw new TimeoutException("wait timed out");
                }
            } while(res == null);
            lockMap.remove(key);
            internalMap.remove(key);
            return res;
        }
    }
}