package Solver;

import GUI.GUI_Graph;

public class Edge {

	/*
	 * Die Kapazität dieser Kante
	 */
	private float capacity;

	/*
	 * Quellknoten der Kante. Der Knoten aus dem diese Kante ausgeht.
	 */
	private Vertex source_vertex;

	/*
	 * Zielknoten der Kante. Der Knoten auf den diese Kante gerichtet ist.
	 */
	private Vertex target_vertex;

	/*
	 * Konstruktor, Festlegung von wo die Kante startet und worauf sie gerichtet
	 * ist. Es wird automatisch die Kapazität der Kante anhand der Koordinaten der
	 * Knoten berechnet.
	 */
	public Edge(Vertex source_vertex, Vertex target_vertex) {
		this.source_vertex = source_vertex;
		this.target_vertex = target_vertex;
		if (GUI_Graph.capacity_is_distance)
			capacity = (float) source_vertex.getCoordinates().distance(target_vertex.getCoordinates());
		else
			capacity = 1;
	}

	/*
	 * Die Kapazität der Kante wird zurückgegeben.
	 */
	public float getCapacity() {
		return capacity;
	}

	/*
	 * Die Kapazität der Kante wird gesetzt.
	 */
	public void setCapacity(float capacity) {
		this.capacity = capacity;
	}

	/*
	 * Gibt den Knoten zurück, aus welchem diese Kante ausgeht.
	 */
	public Vertex sourceVertex() {
		return source_vertex;
	}

	/*
	 * Gibt den Knoten zurück, auf welchen diese Kante gerichtet ist.
	 */
	public Vertex targetVertex() {
		return target_vertex;
	}

}