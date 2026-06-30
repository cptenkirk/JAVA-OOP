package com.mycompany.app;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Endpunkt implements Knotenpunkt {

    private final String ortsname;
    private final Map<Integer, LKW> logbuch = new ConcurrentHashMap<>();

    public Endpunkt(String ortsname) {
        this.ortsname = ortsname;

    }

    @Override
    public void lkwEntladung(LKW lkw) {

        logbuch.put(lkw.transportnummer(), lkw);
        System.out.println(ortsname 
        + "Entladung gestartet " + lkw.transportnummer() 
        + "\n" + " Kennzeichen: " + lkw.kennzeichen() 
        + "\n" + " Fracht: " + lkw.fracht() 
        + "\n" + " Gewicht: " + lkw.gewicht());

    }

    public LKW getLog(int transportnummer) {
        return this.logbuch.get(transportnummer);
    }
        
}
