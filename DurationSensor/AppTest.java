package com.mycompany.app;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConcurrentDurationSensorTest {

    private ConcurrentDurationSensor sensor;

    @BeforeEach 
    void setUp() {
        sensor = new ConcurrentDurationSensor();
    }

    @Test 
    void testBasicFunctionality() {
        sensor.register(10);
        sensor.register(20);
        sensor.register(30);

        Duration snapshot = sensor.getSnapShot();

        assertNotNull(snapshot);
        assertEquals(20, snapshot.getAvg());
        assertEquals(30, snapshot.getMax());
        assertEquals(10, snapshot.getMin());
        assertEquals(60, snapshot.getTotal());
    }

    @Test 
    void testThreadSafetyConcurrency() throws InterruptedException {
        int numberOfThreads = 10;
        int registrationsPerThread = 1000;
        long durationToAdd = 50;

        try (ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads)) {
            CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {
                executor.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                        for (int j = 0; j < registrationsPerThread; j++) {
                            sensor.register(durationToAdd);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }

            readyLatch.await();
            startLatch.countDown();
            finishLatch.await(5, TimeUnit.SECONDS);
        }

        long expectedTotal = (long) numberOfThreads * registrationsPerThread * durationToAdd;
        Duration snapshot = sensor.getSnapShot();

        assertEquals(expectedTotal, snapshot.getTotal());
        assertEquals(durationToAdd, snapshot.getMin());
        assertEquals(durationToAdd, snapshot.getMax());
        assertEquals(durationToAdd, snapshot.getAvg());
    }
}
