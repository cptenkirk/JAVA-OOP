# Programmieraufgabe: Threadsichere Schnittstellenimplementierung



## 1. Technische Voraussetzungen
* **Java Version:** JDK 21 
* **Build-Management:** Maven 3.9+
* **Test-Framework:** JUnit 5

---

## 2. Architektur & Komponentenübersicht



Das System trennt Logik und Datenhaltung strikt und setzt auf lose Kopplung über ein zentrales Interface.

| Klasse / Datei | Fachliche Rolle |
| :--- | :--- |
| **`DurationSnapshot.java`** | Unveränderlicher Datencontainer |
| **`ConcurrentDurationSensor.java`** | Threadsichere Implementierung des Sensors |
| **`DurationSensor.java`** |  Schnittstelle für den Sensor zur Registrierung von Messwerten |
| **`Duration.java`** | Schnittstelle für den Ergebnis-Bericht (Statistiken) |
| **`AppTest.java`** | JUnit 5 Integrationstest |



---

## 3. Quellcode

### 3.1 Unveränderlicher Datencontainer (Immutable Record) zur Bereitstellung der berechneten Metriken. (`DurationSnapshot.java`)
```java
package com.mycompany.app;

public record DurationSnapshot(long avg, long max, long min, long total) implements Duration {

    @Override
    public long getAvg() { 
        return avg; 
    }

    @Override
    public long getMax() { 
        return max; 
    }

    @Override
    public long getMin() { 
        return min; 
    }

    @Override
    public long getTotal() { 
        return total; 
    }
}


```

### 3.2 Schnittstelle für den Ergebnis-Bericht (Statistiken) (`Duration.java`)
```java

package com.mycompany.app;

/**
 * Schnittstellte zur Ermittlung von Ausführungsdauern.
 * @author Georg Tsakumagos
 */
public interface Duration {

	/**
	 * Liefert die <em>durchschnittliche</em> Ausführungsdauer.
	 * @return Die minimale Ausführungsdauer in Millisekunden.
	 */
	public long getAvg();

	/**
	 * Liefert die <em>maximale</em> Ausführungsdauer.
	 * @return Die maximale Ausführungsdauer in Millisekunden.
	 */
	public long getMax();

	/**
	 * Liefert die <em>minimale</em> Ausführungsdauer.
	 * @return Die minimale Ausführungsdauer in Millisekunden.
	 */
	public long getMin();
	
	/**
	 * Liefert die aggregierte Dauer aller Ausführungen. Diese Zahl wächst stetig.
	 * @return Die aggregierte Ausführungsdauer in Millisekunden
	 */
	public long getTotal();

}


```

### 3.3 Schnittstelle für den Sensor zur Registrierung von Messwerten. (`DurationSensor.java`)
```java
package com.mycompany.app;
/**
 * Schnittstelle für einen Sensor zur Messung der Ausführungsdauer.
 * 
 * @author Georg Tsakumagos
 */
public interface DurationSensor {

    /**
     * Liefert den aktuellen Zustand der Messungen.
     * 
     * @return Den aktuellen Zustand.
     */
    public Duration getSnapShot();

    /**
     * Registriert eine Ausführungsdauer.
     * 
     * @param durationMS
     *            Die Ausführungsdauer in Millisekunden. 
     */
    public void register(final long durationMS);

}

```

### 3.4 Threadsichere Implementierung des Sensors mittels (`ConcurrentDurationSensor.java`)
```java
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

```

### 3.5 Automatisierter Unit-Test zum Nachweis der mathematischen Korrektheit und Thread-Safety unter paralleler Last. (`AppTest.java`)
```java
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

```

## 4 UnitTest 5 Ergebnis 
```java

PS G:\DEV\JAVA\AUFGABESTAROFIT\my-app> mvn clean test
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------------< com.mycompany.app:my-app >----------------------
[INFO] Building my-app 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- clean:3.4.0:clean (default-clean) @ my-app ---
[INFO] Deleting G:\DEV\JAVA\AUFGABESTAROFIT\my-app\target
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ my-app ---
[INFO] skip non existing resourceDirectory G:\DEV\JAVA\AUFGABESTAROFIT\my-app\src\main\resources
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ my-app ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 4 source files with javac [debug release 21] to target\classes
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ my-app ---
[INFO] skip non existing resourceDirectory G:\DEV\JAVA\AUFGABESTAROFIT\my-app\src\test\resources
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ my-app ---
[INFO] Recompiling the module because of changed dependency.
[INFO] 
[INFO] --- surefire:3.3.0:test (default-test) @ my-app ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.337 s
[INFO] Finished at: 2026-07-01T00:43:05+02:00
[INFO] ------------------------------------------------------------------------
PS G:\DEV\JAVA\AUFGABESTAROFIT\my-app> mvn clean test
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------------< com.mycompany.app:my-app >----------------------
[INFO] Building my-app 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- clean:3.4.0:clean (default-clean) @ my-app ---
[INFO] Deleting G:\DEV\JAVA\AUFGABESTAROFIT\my-app\target
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ my-app ---
[INFO] skip non existing resourceDirectory G:\DEV\JAVA\AUFGABESTAROFIT\my-app\src\main\resources
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ my-app ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 4 source files with javac [debug release 21] to target\classes
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ my-app ---
[INFO] skip non existing resourceDirectory G:\DEV\JAVA\AUFGABESTAROFIT\my-app\src\test\resources
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ my-app ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 1 source file with javac [debug release 21] to target\test-classes
[INFO] 
[INFO] --- surefire:3.3.0:test (default-test) @ my-app ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.mycompany.app.ConcurrentDurationSensorTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.045 s -- in com.mycompany.app.ConcurrentDurationSensorTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.226 s
[INFO] Finished at: 2026-07-01T00:49:21+02:00
[INFO] ------------------------------------------------------------------------

```