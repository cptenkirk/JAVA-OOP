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
