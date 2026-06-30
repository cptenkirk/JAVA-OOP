package com.mycompany.app;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class AppTest {

    // Einfacher Standard-Test
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);

        LKW testLkw = new LKW(99, "Öl", "HBTEST", 10000.0);
        Endpunkt testEndpunkt = new Endpunkt("Fabrik");

        assertNotNull(testLkw, "LKW-Objekt sollte nicht null sein");

        assertEquals(99, testLkw.transportnummer(), "Transportnummer stimmt nicht");
        assertEquals("Öl", testLkw.fracht(), "Die Fracht stimmt nicht");
        assertEquals("HBTEST", testLkw.kennzeichen(), "Das Kennzeichen stimmt nicht");
        assertEquals(10000.0, testLkw.gewicht(), "Gewicht stimmt nicht");

        testEndpunkt.lkwEntladung(testLkw);
    }

    // Testen ob das Logbuch im RAM die Daten behält
    @Test
    public void entladungsLog() {
        LKW tesLkw = new LKW(99, "Öl", "HBTEST", 10000.0);
        Endpunkt testEndpunkt = new Endpunkt("Fabrik");

        testEndpunkt.lkwEntladung(tesLkw);
        
        LKW entladenerLkw = testEndpunkt.getLog(99);
        
        assertNotNull(entladenerLkw, "LKW wurde nicht im Logbuch des Endpunkts gespeichert");
        assertEquals("HBTEST", entladenerLkw.kennzeichen(), "Das Kennzeichen im Logbuch stimmt nicht überein");
        assertEquals("Öl", entladenerLkw.fracht(), "Die Fracht im Logbuch stimmt nicht überein");
    }

    // Der Thread-Safety-Beweis
    @Test
    public void testingThreatSafety() throws Exception {
        
        Endpunkt testEndpunkt = new Endpunkt("Fabrik Multithreading");

        LKW lkw1 = new LKW(10, "Holz", "HBTEST2", 11000.0);
        LKW lkw2 = new LKW(20, "Stahl", "HBTEST3", 30000.0);

      
        CompletableFuture<Void> thread1 = CompletableFuture.runAsync(() -> {
            testEndpunkt.lkwEntladung(lkw1);
        });

        CompletableFuture<Void> thread2 = CompletableFuture.runAsync(() -> {
            testEndpunkt.lkwEntladung(lkw2);
        });

    
        CompletableFuture.allOf(thread1, thread2).join();

        
        assertNotNull(testEndpunkt.getLog(10), "LKW 1 ging verloren");
        assertNotNull(testEndpunkt.getLog(20), "LKW 2 ging verloren");
    }
} 