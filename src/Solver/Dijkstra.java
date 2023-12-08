package Solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class Dijkstra {

	/*
	 * Wendet den Dijkstra Algorithmus auf einen Graphen an und gibt die dadurch
	 * erhaltenen Informationen zu den einzelnen Knoten zurück. Falls nur der
	 * kürzeste Weg zu einem bestimmten Knoten gesucht ist, wird abgebrochen
	 * sobald dieser gefunden wird. Andernfalls wird zu jedem Knoten der kürzeste
	 * Weg bestimmt.
	 */
	public static HashMap<Vertex, dijkstra_object> applyDijkstra(DGraph graph, Vertex source, Vertex sink,
			boolean terminateAtSink) {
		dijkstra_object.resetIDS();

		// Allen Knoten wird die Eigenschaft unbesucht und Entfernung unendlich
		// zugewiesen
		TreeSet<dijkstra_object> vertices = new TreeSet<dijkstra_object>();
		HashMap<Vertex, dijkstra_object> objects_by_vertex = new HashMap<Vertex, dijkstra_object>();
		for (Vertex vertex : graph.getVertices()) {
			if (vertex != source) {
				dijkstra_object object = new dijkstra_object(vertex);
				objects_by_vertex.put(vertex, object);
				vertices.add(object);
			}
		}

		// Startknoten wird besucht
		dijkstra_object source_object = new dijkstra_object(source);
		source_object.setDistance(0);
		source_object.setParent(null);
		objects_by_vertex.put(source, source_object);
		vertices.add(source_object);
		// Algorithmus terminiert dann, wenn alle Knoten besucht wurden
		while (!vertices.isEmpty()) {
			// Der unbesuchte Knoten mit der kleinsten Entfernung wird ausgewählt
			dijkstra_object min_distance = vertices.pollLast();
			// Wird der Zielknoten gefunden wird sofort abgebrochen, falls erwünscht
			if (min_distance.isDistanceInfinite() || (min_distance.getVertex() == sink && !terminateAtSink))
				break;

			min_distance.setVisited();
			// Distanz des Knotens zum Quellknoten wird abgerufen
			float min_capacity = min_distance.getDistance();
			Vertex min_distance_vertex = min_distance.getVertex();

			// Anliegende Knoten werden aktualisiert
			for (Edge edge : min_distance_vertex.getOutgoingEdges()) {
				if (objects_by_vertex.get(edge.targetVertex()).isVisited())
					continue;
				// Distanz vom Quellknoten zu diesem Überknoten über aktuell iterierte
				// Knoten wird berechnet
				float distance = min_capacity + edge.getCapacity();
				dijkstra_object outgoing_vertex_object = objects_by_vertex.get(edge.targetVertex());
				// Aktualisierung falls Distanz geringer
				if (outgoing_vertex_object.isDistanceInfinite() || distance < outgoing_vertex_object.getDistance()) {
					vertices.remove(outgoing_vertex_object);
					outgoing_vertex_object.setDistance(distance);
					outgoing_vertex_object.setParent(min_distance_vertex);
					vertices.add(outgoing_vertex_object);
				}
			}

		}
		return objects_by_vertex;
	}

	/*
	 * Liefert via Backtracking den kürzesten Weg von einem Knoten zum
	 * Quellknoten.
	 */
	public static ArrayList<Vertex> getPathFromParentMap(Vertex sink, HashMap<Vertex, dijkstra_object> parentMap) {
		//Liste aller Knoten des Weges in Reihenfolge
		ArrayList<Vertex> path = new ArrayList<Vertex>(parentMap.size());
		Vertex parent = sink;
		boolean no_way_found = false;
		//Backtracking
		while (parent != null) {
			dijkstra_object o = parentMap.get(parent);
			if (o.hasNoParent()) {
				no_way_found = true;
				break;
			}
			path.add(parent);
			parent = o.getParent();
		}

		if (no_way_found)
			return null;
		return path;
	}

}

class dijkstra_object implements Comparable<dijkstra_object> {

	/*
	 * Objekt um Knoten und seine Werte für den Dijkstra Algorithmus zu speichern.
	 */

	private static double unique_ids = 0;

	public static void resetIDS() {
		unique_ids = 0;
	}

	private Vertex parent;
	private boolean has_no_parent = true;

	private float distance;
	private boolean is_distance_infinite = true;

	private final double id;

	private boolean visited = false;

	private final Vertex vertex;

	/*
	 * Beim Erstellen des Objekts wird dieser automatisch als unbesucht markiert.
	 * Es wird eine Unendliche Distanz zugewiesen als auch, dass dieser Knoten
	 * noch nicht aufgerufen wurde.
	 */
	dijkstra_object(Vertex vertex) {
		this.vertex = vertex;
		id = unique_ids++;
	}

	/*
	 * Gibt den Knoten zurück, für den dieses Objekt Informationen speichert
	 */
	Vertex getVertex() {
		return vertex;
	}

	/*
	 * Gibt den Knoten zurück, der diesen Knoten besucht bzw. aufgerufen hat.
	 */
	Vertex getParent() throws IllegalArgumentException {
		if (has_no_parent)
			throw new IllegalArgumentException("This vertex does not have a parent!");
		return parent;
	}

	/*
	 * Liefert, ob der Knoten von keinem anderen Knoten aufgerufen wurde
	 */
	boolean hasNoParent() {
		return has_no_parent;
	}

	/*
	 * Liefert die Distanz dieses Knotens zum Startknoten. Ist die Distanz
	 * unendlich wird ein Fehler ausgegeben.
	 */
	float getDistance() throws IllegalArgumentException {
		if (is_distance_infinite)
			throw new IllegalArgumentException("The distance is infinite");
		return distance;
	}

	/*
	 * Liefert, ob die Distanz zum Startknoten unendlich ist
	 */
	boolean isDistanceInfinite() {
		return is_distance_infinite;
	}

	/*
	 * Setzt die Distanz dieses Knotens zum Startknoten
	 */
	void setDistance(float distance) {
		this.distance = distance;
		is_distance_infinite = false;
	}

	/*
	 * Speichert von welchem Knoten dieser Knoten aufgerufen wurde
	 */
	void setParent(Vertex parent) {
		this.parent = parent;
		has_no_parent = false;
	}

	/*
	 * Liefert, ob Knoten besucht wurde.
	 */
	boolean isVisited() {
		return visited;
	}

	/*
	 * Markiert Knoten als besucht.
	 */
	void setVisited() {
		visited = true;
	}

	/*
	 * Vergleicht zwei dieser Objekte
	 */
	public int compareTo(dijkstra_object compareObject) {
		if (isDistanceInfinite() && compareObject.isDistanceInfinite())
			return new Double(id).compareTo(compareObject.id);
		if (isDistanceInfinite())
			return -1;
		if (compareObject.isDistanceInfinite())
			return 1;
		int compare_value = new Float(compareObject.getDistance()).compareTo(getDistance());
		if (compare_value == 0)
			return new Double(id).compareTo(compareObject.id);
		else
			return compare_value;
	}

}
