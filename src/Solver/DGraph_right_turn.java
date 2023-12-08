package Solver;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Stack;

public class DGraph_right_turn extends DGraph {

	/*
	 * Überschreibt die Methode aus der Überklasse. Es passiert das gleiche wie in
	 * der Methode der Überklasse nur, dass ein Vertex_right_turn Knoten statt
	 * einem regulären Knoten hinzugefügt wird.
	 */
	public Vertex addVertex(Point coordinates, String name) throws IllegalArgumentException {
		if (name == null)
			throw new IllegalArgumentException("Name must not be null");
		if (vertices_by_name.containsKey(name))
			throw new IllegalArgumentException("Double identifier");
		Vertex ver = new Vertex_right_turn(coordinates, name);
		vertices.add(ver);
		vertices_by_name.put(name, ver);
		return ver;
	}

	/*
	 * Wandelt einen Beliebigabbiegegraph in einen Rechtsabbiegegraph um.
	 */
	public static DGraph_right_turn convert(DGraph_all_directions graph) throws IllegalArgumentException {

		// Überprüfung, ob der umzuwandelnde Graph zulässig ist, also keine
		// Rückkanten besitzt
		if (!graph.getEdges().isEmpty()) {
			Edge test_edge = graph.getEdges().iterator().next();
			if (test_edge.sourceVertex().incomingVerticesContainsVertex(test_edge.targetVertex())) {
				throw new IllegalArgumentException(
						"Ein Graph mit Rückkanten kann nicht konvertiert werden wegen Anomaliegefahr");
			}
		}

		DGraph_right_turn new_graph = new DGraph_right_turn();

		// Knoten des umzuwandelnden Graph in neuen Graph kopieren
		Stack<Vertex> old_vertices = new Stack<Vertex>();
		for (Vertex ver : graph.getVertices()) {
			old_vertices.push(new_graph.addVertex(ver.getCoordinates(), ver.getName()));
		}

		// Aufteilung jedes Knoten in Unterknoten
		Stack<Vertex> createdVertices = new Stack<Vertex>();
		for (Vertex ver : graph.getVertices()) {
			splitVertex(new_graph, graph, ver, createdVertices);
		}

		// Erzeugte Unterknoten untereinander verbinden
		for (Vertex vertex : createdVertices) {
			for (Edge edge : new ArrayList<Edge>(vertex.getOutgoingEdges())) {
				// Bestimmte passenden Unterknoten
				Vertex new_target = null;
				for (Vertex child : ((Vertex_right_turn) edge.targetVertex()).getChildreen()) {
					if (child.incomingVerticesContainsVertex(((Vertex_right_turn) vertex).getParent())) {
						new_target = child;
						break;
					}
				}
				// Passe Kante an
				new_graph.addEdge(vertex, new_target);
				new_graph.removeEdge(edge);
			}
		}

		// Entferne Überknoten aus Graph
		for (Vertex old_ver : old_vertices)
			new_graph.removeVertex(old_ver);

		return new_graph;
	}

	/*
	 * Teilt Knoten in Unterknoten auf.
	 */
	private static void splitVertex(DGraph_right_turn destination_graph, DGraph_all_directions graph, Vertex vertex,
			Stack<Vertex> createdVertices) {

		// Überprüfung, ob Knoten A keine Kanten besitzt
		if (vertex.getOutgoingEdges().size() == 0 && vertex.getIncomingEdges().size() == 0) {
			Vertex_right_turn new_vertex = (Vertex_right_turn) destination_graph.addVertex(vertex.getCoordinates(),
					generateVertexname(vertex.getName(), 0));
			((Vertex_right_turn) destination_graph.getVertex(vertex.getName())).addChild(new_vertex);
		}

		int i = 0;
		/*
		 * Da umzuwandelnder Graph ungerichtet ist, wird zwischen eingehenden und
		 * ausgehenden Kanten nicht differenziert. Es müssen daher ausgehende und
		 * eingehende Kanten abgearbeitet werden.
		 */
		for (Edge edge : vertex.getOutgoingEdges()) {
			// Erzeuge Unterknoten a für Kante von B->A; erlaubte Wege seien A->C und
			// optional A->D (falls geradeaus)
			Vertex_right_turn new_vertex = (Vertex_right_turn) destination_graph.addVertex(vertex.getCoordinates(),
					generateVertexname(vertex.getName(), i++));
			((Vertex_right_turn) destination_graph.getVertex(vertex.getName())).addChild(new_vertex);
			// Erzeuge eingehende Kante von B zu Unterknoten a
			destination_graph.addEdge(destination_graph.getVertex(edge.targetVertex().getName()), new_vertex);
			Edge[] path = vertex.getAllowedEdges(edge);
			// Erzeuge ausgehende Kanten von a zu C und optinal D
			for (Edge path_edge : path) {
				if (path_edge.sourceVertex() == vertex)
					destination_graph.addEdge(new_vertex, destination_graph.getVertex(path_edge.targetVertex().getName()));
				else
					destination_graph.addEdge(new_vertex, destination_graph.getVertex(path_edge.sourceVertex().getName()));
			}
			createdVertices.push(new_vertex);
		}

		for (Edge edge : vertex.getIncomingEdges()) {
			// Erzeuge Unterknoten a für Kante von B->A; erlaubte Wege seien A->C und
			// optional A->D (falls geradeaus)
			Vertex_right_turn new_vertex = (Vertex_right_turn) destination_graph.addVertex(vertex.getCoordinates(),
					generateVertexname(vertex.getName(), i++));
			((Vertex_right_turn) destination_graph.getVertex(vertex.getName())).addChild(new_vertex);
			// Erzeuge eingehende Kante von B zu Unterknoten a
			destination_graph.addEdge(destination_graph.getVertex(edge.sourceVertex().getName()), new_vertex);
			Edge[] path = vertex.getAllowedEdges(edge);
			// Erzeuge ausgehende Kanten von a zu C und optinal D
			for (Edge path_edge : path) {
				if (path_edge.sourceVertex() == vertex)
					destination_graph.addEdge(new_vertex, destination_graph.getVertex(path_edge.targetVertex().getName()));
				else
					destination_graph.addEdge(new_vertex, destination_graph.getVertex(path_edge.sourceVertex().getName()));
			}
			createdVertices.push(new_vertex);
		}

	}

	/*
	 * Erzeuge Knotenname
	 */
	public static String generateVertexname(String vertex_name, int number) {
		return "#" + vertex_name + "." + number;
	}

}