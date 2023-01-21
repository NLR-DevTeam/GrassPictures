package cn.whitrayhb.grasspics.utils;

import java.util.concurrent.ConcurrentHashMap;

public class Cooler {
    private static final ConcurrentHashMap<Long, Long> coolDownMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Long> lockTimeMap = new ConcurrentHashMap<>();

    public static synchronized void lock(long uid, long lockTime) {
        coolDownMap.put(uid, System.currentTimeMillis());
        lockTimeMap.put(uid, lockTime);
    }

    public static synchronized boolean isLocked(long uid) {
        if (!coolDownMap.containsKey(uid) || !lockTimeMap.containsKey(uid)) return false;
        return (System.currentTimeMillis() - coolDownMap.get(uid)) >= lockTimeMap.get(uid);
    }
}
