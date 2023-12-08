package Solver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.TreeSet;

public class Vertices_not_reachable {

	/*
	 * Liefert, falls vorhanden, ein Knotenpaar zurück, was durch das
	 * Linksabbiegeverbot sich nicht gegenseitig erreichen kann.
	 */
	public static Vertex[] ExistVerticesNotReachableBecauseLeftProhibition(DGraph_all_directions graph_all) {
		// Unterteilung in zusammenhängende Graphen
		Stack<DGraph_all_directions> splitGraphs = Split_graph.splitGraph(graph_all);
		for (DGraph_all_directions graph : splitGraphs) {
			// Erstelle Rechtsabbiegegraph aus zusammenhängendem Graph
			DGraph_right_turn graph_right = DGraph_right_turn.convert(graph);
			// Prüfung, ob ein Knotenpaar existiert, was im Rechtsabbiegegraph nicht
			// gegenseitig erreichbar ist.
			// In einem zusammenhängendem Graph ohne Linksabbiegeverbot können alle
			// Knoten sich gegenseitig erreichen
			Stack<Vertex[]> non_reachable_right = VerticesWhichCantReachEachOther(graph_right, false);
			if (!non_reachable_right.isEmpty())
				return new Vertex[] { non_reachable_right.peek()[0], non_reachable_right.peek()[1] };

		}
		return null;
	}

	/*
	 * Gibt alle oder nur ein Beispiel von Knoten zurück, die sich nicht
	 * gegenseitig erreichen können.
	 */
	public static Stack<Vertex[]> VerticesWhichCantReachEachOther(DGraph graph, boolean find_all_pairs) {
		// Speichert alle Knotenpaare, die sich gegenseitig nicht erreichen können
		Stack<Vertex[]> non_reachable_vertices = new Stack<Vertex[]>();
		// Alle Überknoten werden gespeichert
		HashSet<Vertex> hierarchie_tops = new HashSet<Vertex>();
		for (Vertex vertex : graph.getVertices()) {
			if (!hierarchie_tops.contains(vertex.getHierarchieTop()))
				hierarchie_tops.add(vertex.getHierarchieTop());
		}
		// Alle Zyklen des Graphen werden zusammengefasst
		Remove_Cycles.removeAllCycles(graph);
		// Speichert alle Knoten, die erreichbar von einem Knoten sind
		HashMap<Vertex, HashSet<Vertex>> iterated_list = new HashMap<Vertex, HashSet<Vertex>>();

		int percent = hierarchie_tops.size() / 100;
		if (percent == 0)
			percent = 1;
		int progress = 0;
		// Liste von Knotengruppen (mehrere Knoten) von denen man alle Kreuzungen
		// erreichen kann
		HashSet<String> set_of_nodes_which_can_reach_all_other_vertices = new HashSet<String>();
		for (Vertex vertex : hierarchie_tops) {
			if (progress++ % percent == 0) {
				GUI.Frame.algorithm_progress.setValue(progress / percent);
			}
			// Durch Entfernen von Zyklen sind bestimmte Knoten verschmolzen.
			// Bestimmen in welcher Knotengruppe sich alle Unterknoten befinden
			Stack<Vertex> relatives_locations = new Stack<Vertex>();
			TreeSet<String> serializedRelatives = new TreeSet<String>();
			for (Vertex relative : vertex.getHierarchieEnd()) {
				Vertex currentLocation;
				if (!graph.getVertices().contains(relative))
					currentLocation = graph.getMergedVertex(relative);
				else
					currentLocation = relative;
				relatives_locations.push(currentLocation);
				serializedRelatives.add(currentLocation.getName());
			}
			// Erstellt Zeichenkette, due repräsentiert in welchen Knotengruppen sich
			// die Unterknoten des Überknoten befinden
			String vertex_distribution = "";
			for (String string : serializedRelatives.descendingSet())
				vertex_distribution += string + "/";

			if (set_of_nodes_which_can_reach_all_other_vertices.contains(vertex_distribution))
				continue;
			// Es werden alle Überknoten ermittelt, die von der aktuellen Knotengruppe
			// aus erreichbar sind
			HashSet<Vertex> reachable = getReachableVerticesFromVertexGroup(graph, relatives_locations, iterated_list);
			// Überprüfung, ob ein Überknoten nicht erreicht wurde
			if (reachable.size() < hierarchie_tops.size()) {
				HashSet<Vertex> non_reachable = new HashSet<Vertex>(hierarchie_tops);
				non_reachable.removeAll(reachable);
				// Rückgabe vom nicht gegenseitig erreichbaren Knotenpaar
				if (!find_all_pairs) {
					GUI.Frame.algorithm_progress.setValue(100);
					non_reachable_vertices.push(new Vertex[] { vertex, non_reachable.iterator().next() });
					return non_reachable_vertices;
				} else {
					// Speichern aller nicht gegenseitig erreichbaren Knoten
					for (Vertex not_reached : non_reachable)
						non_reachable_vertices.push(new Vertex[] { vertex, not_reached });
				}
			} else
				set_of_nodes_which_can_reach_all_other_vertices.add(vertex_distribution);

		}

		GUI.Frame.algorithm_progress.setValue(100);

		return non_reachable_vertices;
	}

	/*
	 * Liefert alle Überknoten, die von einer Knotengruppe erreichbar sind.
	 */
	private static HashSet<Vertex> getReachableVerticesFromVertexGroup(DGraph graph, Stack<Vertex> source,
			HashMap<Vertex, HashSet<Vertex>> iterated_list) {

		HashSet<Vertex> visited_vertices = new HashSet<Vertex>();
		visited_vertices.addAll(source);
		// Breitensuche ausgehend von jedem Startunterknoten
		for (Vertex vertex : source) {

			if (iterated_list.containsKey(vertex)) {
				visited_vertices.addAll(iterated_list.get(vertex));
				continue;
			}

			HashSet<Vertex> iteration_list = new HashSet<Vertex>();
			iteration_list.add(vertex);
			Stack<Vertex> in_queue = new Stack<Vertex>();
			// Liste wird mit allen Knotengruppen gefüllt erreichbar von Startknoten
			// einer Breitensuche
			HashSet<Vertex> reference_to_source_set = new HashSet<Vertex>();
			iterated_list.put(vertex, reference_to_source_set);
			// Breitensuche
			while (!iteration_list.isEmpty()) {

				for (Vertex next_vertex : iteration_list) {
					for (Edge edge : next_vertex.getOutgoingEdges()) {
						if (reference_to_source_set.contains(edge.targetVertex()))
							continue;

						visited_vertices.add(edge.targetVertex());

						reference_to_source_set.add(edge.targetVertex());
						// Falls möglich übernehmen von Informationen aus vorherigen
						// Breitensuchen
						if (iterated_list.containsKey(edge.targetVertex())) {
							reference_to_source_set.addAll(iterated_list.get(edge.targetVertex()));
							visited_vertices.addAll(iterated_list.get(edge.targetVertex()));
						} else
							in_queue.push(edge.targetVertex());

					}
				}

				iteration_list.clear();
				iteration_list.addAll(in_queue);
				in_queue.clear();

			}

		}
		// Ermitteln aller erreichten Überknoten
		HashSet<Vertex> reachable = new HashSet<Vertex>();
		for (Vertex vertex : visited_vertices) {
			reachable.add(vertex.getHierarchieTop());
			reachable.addAll(vertex.getMergedTops());
		}

		return reachable;
	}

}
