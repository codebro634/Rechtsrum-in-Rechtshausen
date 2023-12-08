package Solver;

import GUI.GUI_Graph;

public class Edge {

	/*
	 * Die Kapazit�t dieser Kante
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
	 * ist. Es wird automatisch die Kapazit�t der Kante anhand der Koordinaten der
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
	 * Die Kapazit�t der Kante wird zur�ckgegeben.
	 */
	public float getCapacity() {
		return capacity;
	}

	/*
	 * Die Kapazit�t der Kante wird gesetzt.
	 */
	public void setCapacity(float capacity) {
		this.capacity = capacity;
	}

	/*
	 * Gibt den Knoten zur�ck, aus welchem diese Kante ausgeht.
	 */
	public Vertex sourceVertex() {
		return source_vertex;
	}

	/*
	 * Gibt den Knoten zur�ck, auf welchen diese Kante gerichtet ist.
	 */
	public Vertex targetVertex() {
		return target_vertex;
	}

}