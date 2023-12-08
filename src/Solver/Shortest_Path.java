package Solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Shortest_Path {

	/*
	 * Liefert den kürzesten Weg zwischen zwei Kreuzungen
	 */
	public static ArrayList<Vertex> shortest_path(DGraph graph, Vertex source, Vertex sink) {
		GUI.Frame.algorithm_progress.setValue(0);
		// Füge imaginäre Start- und Zielknoten hinzu
		Vertex[] source_and_sink = setupDijkstra(graph, source.getHierarchieEnd(), sink.getHierarchieEnd());
		GUI.Frame.algorithm_progress.setValue(20);
		// Wende den Dijkstra Algorithmus auf Graphen an
		HashMap<Vertex, dijkstra_object> parentMap = Dijkstra.applyDijkstra(graph, source_and_sink[0], source_and_sink[1],
				true);
		GUI.Frame.algorithm_progress.setValue(40);
		// Der kürzeste Weg wird aus den erhaltenen Daten des Dijkstras extrahiert
		ArrayList<Vertex> path = Dijkstra.getPathFromParentMap(source_and_sink[1], parentMap);
		if (path != null) {
			path.remove(0);
			path.remove(path.size() - 1);
		}
		GUI.Frame.algorithm_progress.setValue(60);
		// Originalzustand des Graphen wird wieder hergestellt
		graph.removeVertex(source_and_sink[0]);
		GUI.Frame.algorithm_progress.setValue(80);
		graph.removeVertex(source_and_sink[1]);
		GUI.Frame.algorithm_progress.setValue(100);
		return path;
	}

	/*
	 * Fügt einem Graphen zwei imaginäre Knoten hinzu, welche als Start- und
	 * Zielknoten für den Dijkstra Algorithmus dienen. Gibt die hinzugefügten
	 * Knoten zurück.
	 */
	public static Vertex[] setupDijkstra(DGraph graph, Stack<Vertex> source, Stack<Vertex> sink) {
		Vertex addedSource = graph.addVertex(source.peek().getCoordinates(), "#Source", true);
		Vertex addedSink = graph.addVertex(sink.peek().getCoordinates(), "Sink", true);

		// Verbindung mit allen Startunterknoten
		for (Vertex vertex : source) {
			if (!graph.getVertices().contains(vertex))
				continue;
			Edge edge = graph.addEdge(addedSource, vertex);
			edge.setCapacity(0);
		}

		// Verbindung mit allen Zielunterknoten
		for (Vertex vertex : sink) {
			if (!graph.getVertices().contains(vertex))
				continue;
			Edge edge = graph.addEdge(vertex, addedSink);
			edge.setCapacity(0);
		}

		return new Vertex[] { addedSource, addedSink };
	}

}
