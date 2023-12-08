package Solver;

public class difference_object {

	/*
	 * Klasse repräsentiert den Unterschied
	 * des kürzesten Weges zwischen zwei Knoten.
	 */
	
	private boolean no_difference = false;
	
	private boolean infinite_difference = true;
	
	private float difference;

	private Vertex from, to;
	
	/*
	 * Das Objekt wird intialisiert. Das Knotenpaar, welches
	 * diesen Unterschied bildet, wird gespeichert.
	 */
	difference_object(Vertex from, Vertex to){
		this.from = from;
		this.to = to;
	}
	
	/*
	 * Liefert den Unterschied. Ist dieser 1 
	 * oder unendlich wird ein Fehler ausgegeben.
	 */
	public float getDifference() throws Exception{
		if(no_difference || infinite_difference) throw new Exception("Unterschied ist entweder unendlich oder nicht vorhanden!");
		return difference;
	}

	/*
	 * Setzt den Unterschied, den dieses Objekt speichert.
	 */
	public void setDifference(float difference){
		infinite_difference = false;
		if(difference == 1){
			no_difference = true;
		}
		else{
			no_difference = false;
		}
		this.difference = difference;
	}
	
	/*
	 * Liefert, ob der gepseicherte Unterschied unendlich ist.
	 */
	public boolean isDifferenceInfinite(){
		return infinite_difference;
	}
	
	/*
	 * Liefert, ob der gespeicherte Unterschied 1 ist.
	 */
	public boolean noDifference(){
		return no_difference;
	}
	
	/*
	 * Liefert den Startknoten.
	 */
	public Vertex getFrom(){
		return from;
	}
	
	/*
	 * Liefert den Zielknoten.
	 */
	public Vertex getTo(){
		return to;
	}
	
	/*
	 * Vergleicht zwei Objekte nach Unterschied.
	 */
	public int compareTo(difference_object o){
		if((o.isDifferenceInfinite() && isDifferenceInfinite()) || (o.noDifference() && noDifference())) return 0;
		if(o.isDifferenceInfinite()) return 1;
		if(isDifferenceInfinite()) return -1;
		if(o.noDifference()) return -1;
		if(noDifference()) return 1;
		try {
			return new Float(o.getDifference()).compareTo(getDifference());
		} catch (Exception e) {}
		return 0;
	}
	
}
