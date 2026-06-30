package com.mycompany.app;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentDurationSensor implements DurationSensor {

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwl.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwl.writeLock();

    // Interner Zustand der Messungen
    private long count = 0;
    private long total = 0;
    private long max = 0;
    private long min = Long.MAX_VALUE;

    @Override
    public void register(final long durationMS) {
        if (durationMS < 0) {
            throw new IllegalArgumentException("Dauer darf nicht negativ sein.");
        }

        writeLock.lock();
        try {
            count++;
            total += durationMS;
            if (durationMS > max) {
                max = durationMS;
            }
            if (durationMS < min) {
                min = durationMS;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Duration getSnapShot() {
        readLock.lock();
        try {
            long currentMin = (count == 0) ? 0 : min;
            long currentAvg = (count == 0) ? 0 : (total / count);

            
            return new DurationSnapshot(currentAvg, max, currentMin, total);
        } finally {
            readLock.unlock();
        }
    }
}

