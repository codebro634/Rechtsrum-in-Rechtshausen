package Solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class Shortest_Path_illegal_turns {

	/*
	 * Liefert den kürzesten Weg zwischen zwei Knoten mit einer variablen Anzahl
	 * an erlaubten falschen Abbiegen.
	 */
	public static ArrayList<Vertex> getPath(DGraph_all_directions graph_all, DGraph_right_turn graph_right, Vertex source,
			Vertex target, int illegal_turns) {
		GUI.Frame.algorithm_progress.setValue(0);
		@SuppressWarnings("unchecked")
		// Speichere alle anfänglichen Unterknoten des Startknoten
		Stack<Vertex> originalSources = (Stack<Vertex>) source.getHierarchieEnd().clone();
		// Erzeuge Ebenen (Kopien des Graphen)
		ArrayList<HashSet<Vertex>> createdLayers = createLayers(graph_right, illegal_turns);
		GUI.Frame.algorithm_progress.setValue(20);
		// Verbinde Knoten mit Zielknoten auf nächst tieferer Ebene durch Kanten,
		// die falsches Abbiegen repräsentieren
		connectLayers(graph_all, graph_right, createdLayers);
		GUI.Frame.algorithm_progress.setValue(40);
		// Füge imaginären Startknoten und Zielknoten hinzu
		Vertex[] frameVertices = Shortest_Path.setupDijkstra(graph_right, originalSources, target.getHierarchieEnd());
		GUI.Frame.algorithm_progress.setValue(60);
		// Dijkstra Algorithmus
		HashMap<Vertex, dijkstra_object> results = Dijkstra.applyDijkstra(graph_right, frameVertices[0], frameVertices[1],
				true);
		GUI.Frame.algorithm_progress.setValue(100);
		// Erhalte den Weg aus den Ergebnissen des Dijkstra Algorithmus
		ArrayList<Vertex> path = Dijkstra.getPathFromParentMap(frameVertices[1], results);
		if (path != null) {
			// Falls ein Weg gefunden wurde, werden die temporären Start und
			// Zielknoten wieder entfernt
			path.remove(0);
			path.remove(path.size() - 1);
		}
		return path;
	}

	/*
	 * Erzeugt eine Kopie aller Kanten und Knoten innerhalb des Graphen so oft wie
	 * man falsch Abbiegen darf. Die Namen der Knoten aller Kopien unterscheiden
	 * sich durch ein zusätzliches # am Anfang ihres Namens.
	 */
	private static ArrayList<HashSet<Vertex>> createLayers(DGraph_right_turn graph, int illegal_turns) {
		// Speichern aller Knoten, die in den Ebenen enthalten sind
		ArrayList<HashSet<Vertex>> layer_vertices = new ArrayList<HashSet<Vertex>>(illegal_turns + 1);
		layer_vertices.add(new HashSet<Vertex>(graph.getVertices()));
		StringBuilder prefix_builder = new StringBuilder(illegal_turns);
		ArrayList<Vertex> vertices = new ArrayList<Vertex>(graph.getVertices());
		ArrayList<Edge> edges = new ArrayList<Edge>(graph.getEdges());
		// Für jede Ebene
		for (int i = 0; i < illegal_turns; i++) {
			prefix_builder.append("#");
			String prefix = prefix_builder.toString();
			HashSet<Vertex> layer_set = new HashSet<Vertex>();
			// Füge Knoten der neuen Ebenen hinzu
			for (Vertex vertex : vertices) {
				Vertex addedVertex = graph.addVertex(vertex.getCoordinates(), prefix + vertex.getName());
				Vertex_right_turn parent = (Vertex_right_turn) vertex.getHierarchieTop();
				parent.addChild((Vertex_right_turn) addedVertex);
				layer_set.add(addedVertex);
			}
			// Füge Kanten der neuen Ebene hinzu
			for (Edge edge : edges)
				graph.addEdge(graph.getVertex(prefix + edge.sourceVertex().getName()),
						graph.getVertex(prefix + edge.targetVertex().getName()));
			layer_vertices.add(layer_set);
		}
		// Hier existieren jetzt Kopien ohne Verbindungen untereinander
		return layer_vertices;
	}

	/*
	 * Verbindet die verschiedenen Ebenen des Graphen durch Kanten von Unterknoten
	 * zu Unterknoten in der nächsten Ebene, die illegales Abbiegen
	 * repräsentieren.
	 */
	private static void connectLayers(DGraph_all_directions graph_all, DGraph_right_turn graph_right,
			ArrayList<HashSet<Vertex>> layers) {
		// Ist die Liste nur 1 groß, wurden keine Kopien erzeugt und es kann auch
		// nichts verbunden werden
		if (layers.size() < 2)
			return;
		// Bilden des Präfix des tiefsten Ebene - Teilstring davon werden zum
		// schnellen Zugriff benötigt
		StringBuilder builder_last_layer_prefix = new StringBuilder(layers.size());
		for (int i = 0; i < layers.size(); i++)
			builder_last_layer_prefix.append("#");
		String last_layer_prefix = builder_last_layer_prefix.toString();
		// Für jede Ebene mit Nachfolger
		for (int i = layers.size() - 2; i >= 0; i--) {
			// Für jeden Knoten jeder Ebene
			for (Vertex vertex : layers.get(i)) {
				if (vertex.getIncomingEdges().isEmpty())
					continue;
				// Ermitteln zu welchen Überknoten das Abbiegen eigentlich verboten ist
				Vertex parentVertex = graph_all.getVertex(vertex.getHierarchieTop().getName());
				Vertex incomingVertexParent = graph_all
						.getVertex(vertex.getIncomingEdges().iterator().next().sourceVertex().getHierarchieTop().getName());
				Edge incomingEdge = parentVertex.getIncomingEdge(incomingVertexParent);
				if (incomingEdge == null)
					incomingEdge = parentVertex.getOutgoingEdge(incomingVertexParent);
				Edge[] prohibitedEdges = parentVertex.getProhibitedEdges(incomingEdge);

				// Erzeuge Kante zu Unterknoten in nächster Ebene zu denen Abbiegen
				// verboten ist
				for (Edge edge : prohibitedEdges) {
					Vertex target_ver = null;
					if (edge.sourceVertex() == parentVertex)
						target_ver = edge.targetVertex();
					else
						target_ver = edge.sourceVertex();

					String prefix = last_layer_prefix.substring(0, i + 2);
					Vertex_right_turn nextLayerVer = null;
					int count = 0;
					// Suche korrekten Unterknoten für diese Kante
					do {
						nextLayerVer = (Vertex_right_turn) graph_right.getVertex(prefix + target_ver.getName() + "." + (count++));
						if (nextLayerVer != null && nextLayerVer.getIncomingEdges().iterator().next().sourceVertex()
								.getHierarchieTop().getName().equals(parentVertex.getName())) {
							graph_right.addEdge(vertex, nextLayerVer);
							break;
						}
					} while (nextLayerVer != null);
				}
			}
		}
	}

}
