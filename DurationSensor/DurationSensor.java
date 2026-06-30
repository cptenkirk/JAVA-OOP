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