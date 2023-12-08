package BwInf;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import javax.swing.JOptionPane;

import GUI.Random_graph_frame;
import Solver.DGraph;
import Solver.DGraph_all_directions;
import Solver.DGraph_right_turn;
import Solver.Edge;
import Solver.Vertex;
import Solver.Vertices_not_reachable;

public abstract class MapGenerator {

	public static void generateRandomMap() {
		// Erhalte Benutzereingaben
		Random_graph_frame.displayFrame();
	}

	/*
	 * Erzeugt einen zuf�lligen Graph mit gegebenen Rahmenbedingungen, welcher in
	 * das GUI geladen und optional in eine Datei gepseichert wird.
	 */
	public static void generateMap(int x_range, int y_range, int vertex_count, int edge_count, long seed) {
		final Random generator = new Random(seed);

		DGraph_all_directions random_graph = new DGraph_all_directions();

		// Generiere zuf�llig platzierte Knoten
		generateVertices(generator, random_graph, x_range, y_range, vertex_count);

		// Generiere zuf�llig verbundene Kanten, die
		// sich nicht �berschneiden
		addEdges(generator, random_graph, edge_count);

		// Passe Graphen so an, dass alle Knoten sich gegenseitig
		// erreichen k�nnen
		makeAllVerticesReachable(random_graph);

		// Nur f�r GUI:
		// Gebe erzeugten Graphen aus
		Control.dgad = random_graph;
		Control.original_graph = Control.dgad.copy();
		Control.original_graph_right = DGraph_right_turn.convert(Control.original_graph);

		if (GUI.Frame.show_split_graph.isSelected()) {
			GUI.GUI_Graph.displayDGraph(Control.original_graph_right, true);
		} else {
			GUI.GUI_Graph.displayDGraph(Control.original_graph, true);
		}

		// Speichern ,falls erw�nscht, in Datei
		try {
			if (JOptionPane.showConfirmDialog(null, "Erzeugten Graphen speichern?") == 0)
				DGraph.save(random_graph, JOptionPane.showInputDialog("Wie soll die Datei mit dem erzeugten Graphen hei�en?"));
		} catch (Exception e) {
		}

	}

	/*
	 * Passt Graph so an, dass sich alle Knoten gegenseitig erreichen k�nnen.
	 */
	private static void makeAllVerticesReachable(DGraph_all_directions graph) {
		Stack<Vertex[]> pairs = null;
		// Solange Knoten existieren, die sich nicht gegenseitig erreichen k�nnen
		// werden Knoten entfernt
		do {
			// Bestimme alle Knoten, die sich nicht gegenseitig erreichen k�nnen
			pairs = Vertices_not_reachable.VerticesWhichCantReachEachOther(Solver.DGraph_right_turn.convert(graph), true);
			if (pairs.isEmpty())
				continue;
			HashMap<Vertex, Integer> appearances = new HashMap<Vertex, Integer>();
			// Z�hle wie oft jeder Knoten in der Liste vorkommt
			for (Vertex[] pair : pairs) {
				for (Vertex vertex : pair) {
					if (!appearances.containsKey(vertex))
						appearances.put(vertex, 1);
					else
						appearances.put(vertex, appearances.get(vertex) + 1);
				}
			}

			int highest_value = -1;
			for (Map.Entry<Vertex, Integer> entry : appearances.entrySet()) {
				if (entry.getValue() > highest_value)
					highest_value = entry.getValue();
			}
			// Entferne alle Knoten aus dem Graphen, die am h�ufigsten in der Liste
			// vorkommen
			for (Map.Entry<Vertex, Integer> entry : appearances.entrySet()) {
				if (entry.getValue() == highest_value)
					graph.removeVertex(graph.getVertex(entry.getKey().getName()));
			}
		} while (!pairs.isEmpty());
	}

	/*
	 * Erzeuge zuf�llige, sich nicht �berschneidende Kanten.
	 */
	private static void addEdges(final Random generator, DGraph_all_directions random_graph, int edge_count) {
		ArrayList<String> vertex_names = new ArrayList<String>(random_graph.getVertices().size());
		for (Vertex vertex : random_graph.getVertices())
			vertex_names.add(vertex.getName());
		Collections.sort(vertex_names);
		int iterations = 0;
		while (random_graph.getEdges().size() < edge_count) {
			if (iterations++ >= 1000)
				break;
			// Erhalte zwei zuf�llige Knoten
			Vertex source = random_graph.getVertex(vertex_names.get((int) (generator.nextFloat() * vertex_names.size())));
			Vertex target = random_graph.getVertex(vertex_names.get((int) (generator.nextFloat() * vertex_names.size())));
			if (target == source)
				continue;
			// Erhalte Liste an Knoten, die Kante zwischen den beiden zuf�lligen
			// Knoten schneidet
			ArrayList<Vertex> intersection_list = getIntersectingVertices(random_graph, new Edge(source, target));
			// Erzeuge Kanten zwischen allen Knoten in der Liste
			for (int j = 0; j < intersection_list.size() - 1; j++) {
				boolean intersecting_edge = false;
				// Erzeuge Kante, dann wenn sie keine andere Kante des Graphen schneidet
				for (Edge edge : random_graph.getEdges()) {
					if (edge.sourceVertex() == intersection_list.get(j) || edge.targetVertex() == intersection_list.get(j + 1)
							|| edge.sourceVertex() == intersection_list.get(j + 1) || edge.targetVertex() == intersection_list.get(j))
						continue;
					if (new Line2D.Float(edge.sourceVertex().getCoordinates(), edge.targetVertex().getCoordinates())
							.intersectsLine(new Line2D.Float(intersection_list.get(j).getCoordinates(),
									intersection_list.get(j + 1).getCoordinates()))) {
						intersecting_edge = true;
						break;
					}
				}
				// F�ge Kante hinzu
				if (!intersecting_edge
						&& !intersection_list.get(j).incomingVerticesContainsVertex(intersection_list.get(j + 1))) {
					if (random_graph.addEdge(intersection_list.get(j), intersection_list.get(j + 1)) != null)
						iterations = 0;
				}
			}
		}
	}

	/*
	 * Liefert eine Liste an Knoten, die auf einer Kante liegen. Da der Generator
	 * nur ganzzahlige Koordinaten liefert, muss nur mittles ggT das minimale
	 * deltaX und deltaY gebildet werden und dann vom Start mit diesem delta alle
	 * Zwischenpunkten abgearbeitet werden.
	 */
	private static ArrayList<Vertex> getIntersectingVertices(DGraph graph, Edge edge) {
		ArrayList<Vertex> intersection_list = new ArrayList<Vertex>(graph.getVertices().size());
		intersection_list.add(edge.sourceVertex());
		// deltaX und deltaY der Kante bestimmen
		int deltaX = (int) (edge.targetVertex().getCoordinates().getX() - edge.sourceVertex().getCoordinates().getX());
		int deltaY = (int) (edge.targetVertex().getCoordinates().getY() - edge.sourceVertex().getCoordinates().getY());
		// ggT bestimmen und deltaX und deltaY durch ggT teilen
		// falls ggT=0 (deltaX = 0 || deltaY = 0) wird nur die jeweilige Achse
		// betrachtet
		int ggT = ggT(Math.abs(deltaX), Math.abs(deltaY));
		if (ggT > 0) {
			deltaX /= ggT;
			deltaY /= ggT;
		} else {
			if (deltaX == 0) {
				deltaY /= Math.abs(deltaY);
			} else {
				deltaX /= Math.abs(deltaX);
			}
		}
		// Startwerte setzten
		int x = edge.sourceVertex().getCoordinates().x;
		int y = edge.sourceVertex().getCoordinates().y;
		// Alle Zwischenpunkte bis zum Ziel erzeugen
		while (x != edge.targetVertex().getCoordinates().x || y != edge.targetVertex().getCoordinates().y) {
			x += deltaX;
			y += deltaY;
			if (graph.getVertex(x + "/" + y) != null)
				intersection_list.add(graph.getVertex(x + "/" + y));
		}
		return intersection_list;
	}

	/*
	 * Liefert den gr��ten gemeinsamen Teiler zweier Zahlen (Euklidischer
	 * Algorithmus).
	 */
	private static int ggT(int i, int j) {
		if (i == 0 || j == 0)
			return -1;
		int k = -1;
		while (k != 0) {
			k = i % j;
			i = j;
			j = k;
		}
		return i;
	}

	/*
	 * Erzeugt zuf�llig platzierte Knoten.
	 */
	private static void generateVertices(final Random generator, DGraph_all_directions random_graph, int x_range,
			int y_range, int vertex_count) {
		// Bei weniger als H�lfte der m�glichen Knotenplatzierungen werden Knoten
		// zuf�llig platziert
		if (vertex_count < x_range * y_range / 2) {
			HashSet<String> placedVertices = new HashSet<String>();
			for (int i = 0; i < vertex_count; i++) {
				int x, y;
				String serializedPos;
				do {
					x = (int) (generator.nextFloat() * x_range);
					y = (int) (generator.nextFloat() * y_range);
					serializedPos = x + "/" + y;
				} while (placedVertices.contains(serializedPos));
				placedVertices.add(serializedPos);
				random_graph.addVertex(new Point(x, y), serializedPos, false);
			}
		} else {
			// Bei mehr als H�lfte der m�glichen Knotenplatzierungen werden zun�chst
			// allen m�glichen Platzierungen gesetzt und dann solange Platzierungen
			// entfernt bis die die gew�nschte Anzahl erreicht ist
			for (int i = 1; i <= x_range; i++) {
				for (int j = 1; j <= y_range; j++)
					random_graph.addVertex(new Point(i, j), i + "/" + j, false);
			}
			int remove_count = x_range * y_range - vertex_count;
			for (int i = 0; i < remove_count; i++) {
				Vertex vertex = null;
				while (vertex == null)
					vertex = random_graph.getVertex(
							(int) (generator.nextFloat() * x_range + 1) + "/" + (int) (generator.nextFloat() * y_range + 1));
				random_graph.removeVertex(vertex);
			}
		}
	}

}
