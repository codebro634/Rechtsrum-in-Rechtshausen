package Solver;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class Biggest_Difference_heuristic {

	/*
	 * Liefert heuristisch den größten Unterschied, entstanden durch das
	 * Linksabbiegeverbot, und das dazugehörige Knotenpaar.
	 */
	public static difference_object getBiggestDifference(DGraph_all_directions graph_all, DGraph_right_turn graph_right) {
		GUI.Frame.algorithm_progress.setValue(0);

		// Überprüfung, ob Unterschied im Graphen überhaupt existieren kann
		if (graph_all.getVertices().size() < 2) {
			GUI.Frame.algorithm_progress.setValue(100);
			difference_object substitute = new difference_object(null, null);
			substitute.setDifference(1);
			return substitute;
		}

		// Überprüfung, ob mindestens zwei Knoten durch das Linksabbiegeverbot nicht
		// mehr erreichbar sind
		Vertex[] infinite_difference = Vertices_not_reachable.ExistVerticesNotReachableBecauseLeftProhibition(graph_all);
		if (infinite_difference != null) {
			GUI.Frame.algorithm_progress.setValue(100);
			return new difference_object(infinite_difference[0], infinite_difference[1]);
		}
		graph_all.createInverseEdges();

		int percent = graph_all.getVertices().size() / 100, progress = 0;
		if (percent == 0)
			percent = 1;

		difference_object biggest_difference = null;
		// Iterierung aller Knoten des Graphen
		for (Vertex vertex : new ArrayList<Vertex>(graph_all.getVertices())) {

			if (progress++ % percent == 0)
				GUI.Frame.algorithm_progress.setValue(progress / percent);
			// Größter möglicher Unterschied von diesem Knoten zu einem anderen Knoten
			// wird ermittelt
			difference_object biggest_difference_current_vertex = bfsDifference(graph_all, graph_right, vertex);

			// Speicherung des bisher größten gefundenen Unterschieds
			if (biggest_difference == null || biggest_difference.compareTo(biggest_difference_current_vertex) == 1)
				biggest_difference = biggest_difference_current_vertex;

		}
		GUI.Frame.algorithm_progress.setValue(100);

		return biggest_difference;
	}

	/*
	 * Gibt einen möglichen größten Unterschied von einem Knoten zu einem anderen
	 * Knoten des Graphen zurück.
	 */
	private static difference_object bfsDifference(DGraph_all_directions graph_all, DGraph_right_turn graph_right,
			Vertex source) {

		dijkstra_object.resetIDS();

		// Speichert alle Knoten und ihren Unterschied zum Quellknoten 'source'
		HashMap<Vertex, difference_object> visited_vertices = new HashMap<Vertex, difference_object>();

		ArrayList<Vertex> iteration_list = new ArrayList<Vertex>();
		HashSet<Vertex> in_queue = new HashSet<Vertex>();

		// Vorbereitung Breitensuche
		iteration_list.add(source);
		difference_object source_object = new difference_object(source, source);
		source_object.setDifference(1);
		visited_vertices.put(source, source_object);

		difference_object biggest_difference = null;

		// Speicherung aller Daten für Dijkstra im Beliebigabbiegegraph
		TreeSet<dijkstra_object> vertex_data_all = new TreeSet<dijkstra_object>();
		HashMap<Vertex, dijkstra_object> vertex_data_map_all = new HashMap<Vertex, dijkstra_object>();
		HashMap<Vertex, dijkstra_object> vertex_data_tops_map_all = new HashMap<Vertex, dijkstra_object>();
		// Speicherung aller Daten für Dijkstra im Rechtsabbiegegraph
		TreeSet<dijkstra_object> vertex_data_right = new TreeSet<dijkstra_object>();
		HashMap<Vertex, dijkstra_object> vertex_data_map_right = new HashMap<Vertex, dijkstra_object>();
		HashMap<Vertex, dijkstra_object> vertex_data_tops_map_right = new HashMap<Vertex, dijkstra_object>();

		// Hinzufügen von extra Knoten, verbunden mit allen Unterknoten des
		// Startknotens, als Vorbereitung für den Dijkstra
		Vertex addedVertexAll = bfsDijkstraSetup(graph_all, vertex_data_map_all, vertex_data_all, source);
		Vertex addedVertexRight = bfsDijkstraSetup(graph_right, vertex_data_map_right, vertex_data_right,
				graph_right.getVertex(DGraph_right_turn.generateVertexname(source.getName(), 0)));

		while (!iteration_list.isEmpty()) {

			for (Vertex vertex : iteration_list) {
				// Der Unterschied vom 'source' Knoten zum aktuellen Knoten wird
				// abgerufen
				difference_object difference_of_current_vertex = visited_vertices.get(vertex);
				for (Edge edge : vertex.getOutgoingEdges()) { // Allen Kanten des
																											// aktuellen Knoten
																											// iterieren
					if (visited_vertices.containsKey(edge.targetVertex()))
						continue;
					// Zur aktuell betrachteten Kante wird die Weglänge des 'source'
					// Knoten zum Zielknoten der Kante im Beliebigabbiegegraph wird
					// ermittelt
					dijkstra_object dijk_all = vertex_data_map_all.get(bfsDijkstra(vertex_data_tops_map_all,
							vertex_data_map_all, vertex_data_all, edge.targetVertex()));
					// Zur aktuell betrachteten Kante wird die Weglänge des 'source'
					// Knoten zum Zielknoten der Kante im Rechtsabbiegegraph wird
					// ermittelt
					dijkstra_object dijk_right = vertex_data_map_right
							.get(bfsDijkstra(vertex_data_tops_map_right, vertex_data_map_right, vertex_data_right,
									graph_right.getVertex(DGraph_right_turn.generateVertexname(edge.targetVertex().getName(), 0))
											.getHierarchieTop()));
					// Faktor aus den Weglängen wird gebildet
					difference_object difference_of_neighbor_vertex = getDifferenceObjectFromDijkstra(source, edge.targetVertex(),
							dijk_all, dijk_right);

					// Überprüfung, ob der Unterschied des anliegenden Knoten größter oder
					// gleich dem des aktuellen Knotens ist
					if (difference_of_current_vertex.compareTo(difference_of_neighbor_vertex) != -1) {
						visited_vertices.put(edge.targetVertex(), difference_of_neighbor_vertex);
						// Speicherung des bisher größten gefundenen Unterschieds
						if (biggest_difference == null || biggest_difference.compareTo(difference_of_neighbor_vertex) == 1) {
							biggest_difference = difference_of_neighbor_vertex;
						}
						in_queue.add(edge.targetVertex());
					}
				}
			}
			iteration_list.clear();
			iteration_list.addAll(in_queue);
			in_queue.clear();
		}

		// Ursprungszustand des Graphen wiederherstellen
		graph_all.removeVertex(addedVertexAll);
		graph_right.removeVertex(addedVertexRight);

		if (biggest_difference == null)
			return source_object;
		return biggest_difference;
	}

	/*
	 * Bildet aus zwei Weglängen den Unterschiedsfaktor. Ist eines der
	 * Eingabedijkstraobjekte gleich null ist dies gleich bedeutend damit, dass
	 * der Zielknoten eines Weges, den das Dijkstra Objekt eigentlich
	 * repräsentieren sollte, nicht erreicht werden konnte.
	 */
	private static difference_object getDifferenceObjectFromDijkstra(Vertex source, Vertex sink,
			dijkstra_object dijkstra_object_alldirections, dijkstra_object dijkstra_object_right) {
		difference_object difference = new difference_object(source, sink);
		// Beide Weglängen gleich unendlich = Knoten können sich in keinem Graph
		// erreichen = Faktor 1
		if (dijkstra_object_alldirections == null && dijkstra_object_right == null) {
			difference.setDifference(1);
			return difference;
		}
		// Ist nur eine Weglänge unendlich, ist auch der Faktor unendlich
		if (dijkstra_object_alldirections == null) // Dürfte nicht vorkommen, da
																								// hier mehr erreicht wird als
																								// im Rechtsabbiegegraph
			return difference;
		if (dijkstra_object_right == null)
			return difference;
		// Faktor berechnen
		difference.setDifference(dijkstra_object_right.getDistance() / dijkstra_object_alldirections.getDistance());
		return difference;
	}

	/*
	 * Bereitet Anwendung des Dijkstra Algorithmus vor indem ein Knoten, verbunden
	 * mit allen Unterknoten eines Startknotens, mit Kantenkapazitäten von 0
	 * erstellt werden. Gibt den hinzugefügten Knoten zurück.
	 */
	private static Vertex bfsDijkstraSetup(DGraph graph, HashMap<Vertex, dijkstra_object> vertex_data_map,
			TreeSet<dijkstra_object> vertex_data, Vertex source) {
		// Füge Knoten hinzu
		Vertex addedSource = graph.addVertex(new Point(0, 0), "#Source", true);
		// Verbinde neuen Knoten mit Unterknoten des Startknoten
		for (Vertex vertex : source.getHierarchieEnd()) {
			if (!graph.getVertices().contains(vertex))
				continue;
			Edge edge = graph.addEdge(addedSource, vertex);
			edge.setCapacity(0);
		}
		// Einträge in die Listen für Dijkstra Algorithmus
		dijkstra_object source_object = new dijkstra_object(addedSource);
		source_object.setDistance(0);
		source_object.setParent(null);
		vertex_data_map.put(addedSource, source_object);
		vertex_data.add(source_object);
		return addedSource;
	}

	/*
	 * Anwendung des Dijkstra Algorithmus bis ein bestimmter Knoten gefunden wird.
	 * Führt und verwendet Informationen bezüglich aller bisher erreichter Knoten.
	 * Eingabeparamter:
	 * vertexTops: Überknoten und deren Entfernung zum Ursprungsknoten
	 * vertex_data_map: Unterknoten und deren Entfernung zum Ursprungsknoten
	 * vertex_data: Geordnete Liste mit Entfernungsobjekten
	 * parentTop: Überknoten zu dem Weg gesucht ist
	 */
	private static Vertex bfsDijkstra(HashMap<Vertex, dijkstra_object> vertexTops,
			HashMap<Vertex, dijkstra_object> vertex_data_map, TreeSet<dijkstra_object> vertex_data, Vertex parentTop) {

		// Wurde bereits ein Unterknoten des gesuchten Überknotens gefunden, ist der
		// kürzeste Weg bereits gefunden
		if (vertexTops.containsKey(parentTop))
			return vertexTops.get(parentTop).getVertex();

		while (!vertex_data.isEmpty()) {
			// Unbesuchter Knoten mit geringster Entfernung wird ausgewählt
			dijkstra_object min_distance = vertex_data.last();
			// Ist Knoten gesuchter Knoten wird Algorithmus terminiert
			if (min_distance.getVertex().getHierarchieTop() == parentTop)
				return min_distance.getVertex();

			// Knoten wird besucht
			vertex_data.remove(min_distance);
			min_distance.setVisited();
			// Falls Knoten der Unterknoten mit der geringsten Entfernung ist, wird
			// seine Entfernung im Überknoten eingetragen
			dijkstra_object hierarchieTop = vertexTops.get(min_distance.getVertex().getHierarchieTop());
			if (hierarchieTop == null || min_distance.compareTo(hierarchieTop) == 1) //=1, wenn neuer Weg kleiner als bisheriger Weg
				vertexTops.put(min_distance.getVertex().getHierarchieTop(), min_distance);

			// Distanz dieses Knotens zum Ursprungsknoten
			float distance_of_vertex = min_distance.getDistance();
			Vertex min_distance_vertex = min_distance.getVertex();
			// Distanz zum Urpsrungsknoten wird bei allen anliegenden Knoten
			// aktualisiert
			for (Edge edge : min_distance_vertex.getOutgoingEdges()) {
				// Abrufen der Distanz des anliegenden Knoten
				dijkstra_object nearbyVertexData = vertex_data_map.get(edge.targetVertex());
				// Wurde anliegender Knoten noch nicht entdeckt, wird dieser nun in
				// Listen eingetragen
				if (nearbyVertexData == null) {
					dijkstra_object data = new dijkstra_object(edge.targetVertex());
					vertex_data.add(data);
					vertex_data_map.put(edge.targetVertex(), data);
				}
				// Wurde anliegender Knoten bereits besucht, wird dieser ignoriert
				else if (nearbyVertexData.isVisited())
					continue;
				// Distanz vom Quellknoten zu anliegenden Knoten wird ermittelt
				float distance = distance_of_vertex + edge.getCapacity();
				dijkstra_object outgoing_vertex_object = vertex_data_map.get(edge.targetVertex());
				// Falls Distanz kleiner als die gespeicherte Distanz des Knoten wird
				// aktualisiert
				if (outgoing_vertex_object.isDistanceInfinite() || distance < outgoing_vertex_object.getDistance()) {
					vertex_data.remove(outgoing_vertex_object);
					outgoing_vertex_object.setDistance(distance);
					outgoing_vertex_object.setParent(min_distance_vertex);
					vertex_data.add(outgoing_vertex_object);
				}
			}
		}
		return null;
	}

}
