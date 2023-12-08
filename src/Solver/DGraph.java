package Solver;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public abstract class DGraph {

	/*
	 * Klasse Graph repräsentiert einen kantengewichteten, gerichteten Graphen.
	 */

	/*
	 * Speichert einen Graphen in eine Datei.
	 */
	public static void save(DGraph graph, String save_name) {
		// Ausgabestring erzeugen
		String[] content = new String[graph.getVertices().size() + graph.getEdges().size() + 2];
		content[0] = graph.getVertices().size() + " " + graph.getEdges().size();
		ArrayList<String> list = new ArrayList<String>(graph.getVertices().size());
		for (Vertex ver : graph.getVertices())
			list.add(ver.getName() + " " + ver.getCoordinates().getX() + " " + ver.getCoordinates().getY());
		for (int i = 0; i < list.size(); i++)
			content[i + 1] = list.get(i);
		content[list.size() + 1] = "" + graph.getEdges().size();
		list = new ArrayList<String>(graph.getEdges().size());
		for (Edge edg : graph.getEdges())
			list.add(edg.sourceVertex().getName() + " " + edg.targetVertex().getName());
		for (int i = 0; i < list.size(); i++)
			content[graph.getVertices().size() + 2 + i] = list.get(i);
		// Ausgabestring in Datei schreiben
		writeFile(".\\" + BwInf.Import_graph.DIRECTORY + "\\" + save_name + ".txt", content);
	}

	/*
	 * Schreibt Text in eine Datei. Ist die angegebene Datei nicht vorhanden, wird
	 * diese erzeugt.
	 */
	private static void writeFile(String filename, String[] text) {

		if (filename == null || filename.equals("") || !filename.contains(".")) {
			System.out.println("An error occoured due to filename!");
			return;
		}

		if (text == null || text.length == 0) {
			System.out.println("An error occoured due to text content");
			return;
		}

		File levelFile = new File(filename);
		if (!levelFile.exists()) {
			try {
				levelFile.createNewFile();
				new File(filename).createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileWriter writeFile = null;
		BufferedWriter writer = null;
		try {
			writeFile = new FileWriter(levelFile);
			writer = new BufferedWriter(writeFile);

			for (String s : text) {
				writer.write(s);
				writer.newLine();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (Exception e) {
			}
		}
	}

	/*
	 * Speichert alle Knoten des Graphen.
	 */
	protected final HashSet<Vertex> vertices = new HashSet<Vertex>();

	/*
	 * Das Value bei "vertices_by_name" speichert die gleichen Knoten wie
	 * "vertices". Der Key-Wert enthält die Namen der einzelnen Knoten, um auf
	 * diese schnell über den Namen zugreifen zu können.
	 */
	protected final HashMap<String, Vertex> vertices_by_name = new HashMap<String, Vertex>();

	/*
	 * Alle Knoten des Graphen werden in Form eines HashSets wiedergegeben.
	 */
	public HashSet<Vertex> getVertices() {
		return vertices;
	}

	/*
	 * Die Methode gibt einen Knoten über dessen Wiedererkennungswert zurück. Null
	 * wird zurückgegeben, falls kein Knoten mit dem Namen in dem Graphen
	 * vorhanden ist.
	 */
	public Vertex getVertex(String name) {
		return vertices_by_name.get(name);
	}

	/*
	 * Fügt dem Graphen einen Knoten mit einem Namen, Koordinaten und ob dieser
	 * imaginär ist, hinzu. Falls der angegebene Name bereits einem anderem Knoten
	 * zugehörig ist, wird eine Fehlermeldung geworfen. Der zugefügte Knoten wird
	 * als Objekt zurückgegeben. Ist der Knoten imaginär wird dieser in der
	 * Winkelberechnung nicht berücksichtigt.
	 */
	public Vertex addVertex(Point coordinates, String name, boolean imaginary) throws IllegalArgumentException {
		if (name == null)
			throw new IllegalArgumentException("Name must not be null");
		if (vertices_by_name.containsKey(name))
			throw new IllegalArgumentException("Double identifier");
		Vertex ver = new Vertex(coordinates, name, imaginary);
		vertices.add(ver);
		vertices_by_name.put(name, ver);
		return ver;
	}

	/*
	 * Entfernt einen Knoten aus dem Graphen. Alle Kanten, die mit diesen Knoten verbunden sind,
	 * werden ebenfalls entfernt. Wurde der Knoten entfernt, ist sein Name wieder
	 * nutzbar.
	 */
	public void removeVertex(Vertex vertex) {
		// Ausgehende Kanten entfernen
		for (Edge edg : new ArrayList<Edge>(vertex.getOutgoingEdges()))
			removeEdge(edg);
		// Eingehende Kanten entfernen
		for (Edge edg : new ArrayList<Edge>(vertex.getIncomingEdges()))
			removeEdge(edg);
		// Knoten aus Listen entfernen
		vertices.remove(vertex);
		vertices_by_name.remove(vertex.getName());
	}

	/*
	 * Eine Liste, die alle Kanten des Graphen speichert.
	 */
	private final HashSet<Edge> edges = new HashSet<Edge>();

	/*
	 * Eine Liste aller im Graphen vorhandenen Kanten wird wiedergegeben.
	 */
	public HashSet<Edge> getEdges() {
		return edges;
	}

	/*
	 * Eine Kante ausgehend von "source_vertex" und gerichtet auf "target_vertex"
	 * wird dem Graphen hinzufügt. Falls einer der beiden Knoten nicht im Graphen
	 * vorhanden ist oder wenn der Zielknoten dem Quellknoten entspricht, wird ein
	 * Fehler ausgegeben. Existiert bereits eine Kante ausgehend vom Quellknoten
	 * gerichtet zum Zielknoten, wird die Kante nicht erstellt. Die Methode gibt
	 * die hinzugefügte Kante als Objekt zurück.
	 */
	public Edge addEdge(Vertex source_vertex, Vertex target_vertex) throws IllegalArgumentException {
		if (!vertices.contains(source_vertex) || !vertices.contains(target_vertex))
			throw new IllegalArgumentException("[addEdge] One of the selected vertices does not exist in the graph!");

		if (source_vertex == target_vertex)
			throw new IllegalArgumentException("[addEdge] source vertex equals target vertex");

		if (source_vertex.outgoingVerticesContainsVertex(target_vertex)) {
			return null;
		}

		Edge edge = new Edge(source_vertex, target_vertex);
		edges.add(edge);

		target_vertex.addIncomingEdge(edge);
		source_vertex.addOutgoingEdge(edge);

		return edge;
	}

	/*
	 * Eine Kante wird aus dem Graphen entfernt.
	 */
	public void removeEdge(Edge edg) {
		// Aus Quellknoten entfernen
		edg.sourceVertex().removeOutgoingEdge(edg); 
		// Aus Zielknoten entfernen
		edg.targetVertex().removeIncomingEdge(edg); 
		 // Aus Kantenliste entfernen
		edges.remove(edg);
	}

	/*
	 * Kopiert alle Kanten und Knoten des Graphen. Es handelt sich hierbei nicht
	 * um eine tiefe Kopie!
	 */
	public DGraph_all_directions copy() {
		DGraph_all_directions copy = new DGraph_all_directions();
		for (Vertex vertex : getVertices()) {
			copy.addVertex(vertex.getCoordinates(), vertex.getName(), vertex.isImaginary());
		}
		for (Edge edge : getEdges()) {
			copy.addEdge(copy.getVertex(edge.sourceVertex().getName()), copy.getVertex(edge.targetVertex().getName()));
		}
		return copy;
	}

	/*
	 * Liste mit Knoten und Informationen zu welchem Knoten
	 * diese gemergt wurden.
	 */
	private HashMap<Vertex, Vertex> mergeList = new HashMap<Vertex, Vertex>();
	/*
	 * (Performance) Liste von Knoten und welche Knoten in 
	 * ihm gemergt wurden.
	 */
	private HashMap<Vertex, HashSet<Vertex>> inverseMergeList = new HashMap<Vertex, HashSet<Vertex>>();

	/*
	 * Methode gibt den Knoten zurück in dessen Liste seiner zusammengefügten
	 * Knoten der Eingabeknoten enthalten ist.
	 */
	public Vertex getMergedVertex(Vertex vertex) {
		return mergeList.get(vertex);
	}

	/*
	 * Verschmilzt eine Liste von Knoten mit einem Zielknoten. Ebenfalls werden
	 * alle Kanten, verbunden mit einem der Knoten der Liste, nun mit dem
	 * Zielknoten verbunden.
	 */
	public void mergeVertices(Vertex merge_target, Collection<Vertex> vertices) throws IllegalArgumentException {

		for (Vertex to_merge : vertices) {
			// Falls der Knoten mit sich selber verschmolzen werden soll, wird ein
			// Fehler ausgegeben
			if (to_merge == merge_target)
				throw new IllegalArgumentException("Zielknoten kann nicht mit sich selbst gemergt werden.");
			// Alte, eingehende Kanten von Knoten aus Liste auf Mergeknoten umrichten
			for (Edge edg : new ArrayList<Edge>(to_merge.getIncomingEdges())) {
				removeEdge(edg);
				if (edg.sourceVertex() != merge_target)
					addEdge(edg.sourceVertex(), merge_target);
			}
			// Alte, ausgehende Kanten von Knoten der Liste von Mergeknoten kommend,
			// erzeugen
			for (Edge edg : new ArrayList<Edge>(to_merge.getOutgoingEdges())) {
				removeEdge(edg);
				if (merge_target != edg.targetVertex())
					addEdge(merge_target, edg.targetVertex());
			}
			// Überknoten wird zu Liste der gemergten Überknoten des Mergeknotens
			// hinzugefügt
			merge_target.addMergedVertex(to_merge);
			// Falls der zu mergende Knoten selber Produkt einer Verschmelzung war,
			// werden zu ihm gemergten Überknoten in den Mergeknoten übertragen
			merge_target.getMergedTops().addAll(to_merge.getMergedTops());
			to_merge.getMergedTops().clear();
			// Aktualisierung zu welchem Knoten der zu mergende Knoten gemergt wurde
			putSplitList(to_merge, merge_target);
			// Entfernen des zu verschmelzenden Knotens aus dem Graphen
			removeVertex(to_merge);
		}

	}

	/*
	 * Knoten und seine gemergten Knoten
	 * werden nun eingetragen, dass sie
	 * zu einem bestimmten Knoten
	 * verschmolzen wurden.
	 */
	private void putSplitList(Vertex vertex, Vertex merge_target) {

		if (inverseMergeList.containsKey(vertex)) {
			for (Vertex ver : inverseMergeList.get(vertex))
				addToMap(ver, merge_target);
			inverseMergeList.remove(vertex);
		}

		addToMap(vertex, merge_target);

	}

	/*
	 * Hilfsmethode für obere Methode.
	 * Ein Knoten wird entsprechend
	 * in beide Mergelisten eingetragen.
	 */
	private void addToMap(Vertex element, Vertex map) {
		if (!inverseMergeList.containsKey(map)) {
			HashSet<Vertex> set = new HashSet<Vertex>();
			set.add(element);
			inverseMergeList.put(map, set);
		} else
			inverseMergeList.get(map).add(element);

		mergeList.put(element, map);
	}

}
