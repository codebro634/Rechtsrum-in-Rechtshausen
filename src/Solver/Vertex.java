package Solver;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.TreeMap;

public class Vertex {

	/*
	 * Ist dieser Knoten imaginär oder nicht bzw. soll er relevant zur
	 * Winkelbestimmung sein oder nicht.
	 */
	private final boolean imaginary;

	/*
	 * Name des Knoten. Die Namen des Beliebigabbiegraph entsprechen denen aus der
	 * Einlesedatei.
	 */
	private final String name;

	/*
	 * Koordinaten der Kreuzung
	 */
	private final Point coordinates;

	/*
	 * Konstruktor: Koordinaten : "coordinates" Name : "name"
	 */
	public Vertex(Point coordinates, String name, boolean imaginary) {
		this.name = name;
		this.coordinates = coordinates;
		this.imaginary = imaginary;
	}

	/*
	 * Der Name des Knoten wird zurückgegeben.
	 */
	public String getName() {
		return name;
	}

	/*
	 * Die Koordinaten des Knoten werden zurückgegeben.
	 */
	public Point getCoordinates() {
		return coordinates;
	}

	/*
	 * Gibt zurück, ob der Knoten imaginär ist.
	 */
	public boolean isImaginary() {
		return imaginary;
	}

	/*
	 * Speichert alle Überknoten aller Knoten, die zu zu diesem Knoten
	 * verschmolzen wurden.
	 */
	private HashSet<Vertex> mergedTops = new HashSet<Vertex>();

	/*
	 * Gibt alle Überknoten zurück, die zu diesem Knoten zusammengefasst wurden.
	 */
	public HashSet<Vertex> getMergedTops() {
		return mergedTops;
	}

	/*
	 * Fügt einen Überknoten eines Knotens in die Liste der verschmolzenen
	 * Überknoten ein.
	 */
	void addMergedVertex(Vertex merge) {
		mergedTops.add(merge.getHierarchieTop());
	}

	/*
	 * Gibt den obersten Knoten, der Rangfolge wieder in der sich der Knoten
	 * befindet. Ist der Knoten Teil eines Rechtsabbiegegraph, wird sein
	 * Überknoten zurückgegeben. Ist der Knoten Teil eines Beliebigabbiegegraph,
	 * gibt er sich selber zurück.
	 */
	public Vertex getHierarchieTop() {
		if (this instanceof Vertex_right_turn && ((Vertex_right_turn) this).getParent() != null)
			return ((Vertex_right_turn) this).getParent();
		return this;
	}

	/*
	 * Gibt die untersten Knoten, der Rangfolge wieder in der sich der Knoten
	 * befindet. Ist der Knoten Teil eines Rechtsabbiegegraph und ein Unterknoten,
	 * gibt er alle Knoten, die denselben Überknoten haben wie er selbst. Ist der
	 * Knoten Teil eines Rechtsabbiegegraphen und ein Überknoten gibt er seine
	 * Unterknoten zurück. Ist der Knoten Teil eines Beliebigabbiegegraph gibt er
	 * sich selbst zurück.
	 */
	public Stack<Vertex> getHierarchieEnd() {
		if (!(this instanceof Vertex_right_turn)) {
			Stack<Vertex> end = new Stack<Vertex>();
			end.push(this);
			return end;
		} else {
			Vertex_right_turn me = (Vertex_right_turn) this;
			if (me.getParent() == null) {
				return me.getChildreen();
			} else {
				return me.getRelatives();
			}
		}

	}

	/*
	 * Ein Set, das alle ausgehenden Kanten dieses Knotens speichert.
	 */
	private HashSet<Edge> outgoing_edges = new HashSet<Edge>();

	/*
	 * Speichert alle Zielknoten aller ausgehenden Kanten und das dazugehörige
	 * Kantenobjekt (Performance)
	 */
	private HashMap<Vertex, Edge> outgoing_vertices = new HashMap<Vertex, Edge>();

	/*
	 * Eine Liste aller ausgehenden Kanten wird zurückgegeben.
	 */
	public HashSet<Edge> getOutgoingEdges() {
		return outgoing_edges;
	}

	/*
	 * Es wird zurückgegeben, ob dieser Knoten eine Kante besitzt, die auf einen
	 * bestimmten Knoten gerichtet ist.
	 */
	public boolean outgoingVerticesContainsVertex(Vertex vertex) {
		return outgoing_vertices.containsKey(vertex);
	}

	/*
	 * Rückggabe der Kante kommend von diesem Knoten und gerichtet auf einen
	 * bestimmten Knoten falls vorhanden
	 */
	public Edge getOutgoingEdge(Vertex vertex) {
		return outgoing_vertices.get(vertex);
	}

	/*
	 * Eine Kante, die diesen Knoten als Quellknoten hat, wird in die zugehörigen
	 * Listen eingetragen. Diese Methode wird und darf nur über die addEdge
	 * Methode aus der DGraph-Klasse aufgerufen werden.
	 */
	void addOutgoingEdge(Edge edge) {
		outgoing_edges.add(edge);
		outgoing_vertices.put(edge.targetVertex(), edge);
		addAngle(edge);
	}

	/*
	 * Entfernt eine Kante, die diesen Knoten als Quellknoten hat, aus den
	 * zugeörigen Listen. Diese Methode wird darf nur über die Methode removeEdge
	 * aus der DGraph-Klasse aufgerufen werden.
	 */
	void removeOutgoingEdge(Edge edge) {
		outgoing_edges.remove(edge);
		outgoing_vertices.remove(edge.targetVertex());
		removeAngle(edge);
	}

	/*
	 * Liste, die alle Kanten speichert, die diesen Knoten als Zielknoten haben.
	 */
	private HashSet<Edge> incoming_edges = new HashSet<Edge>();

	/*
	 * Speichert alle Quellknoten aller eingehenden Kanten und das dazugehörige
	 * Kantenobjekt (Performance)
	 */
	private HashMap<Vertex, Edge> incoming_vertices = new HashMap<Vertex, Edge>();

	/*
	 * Liste aller eingehenden Kanten wird zurückgegeben.
	 */
	public HashSet<Edge> getIncomingEdges() {
		return incoming_edges;
	}

	/*
	 * Rückggabe, ob eine Kante kommend von einem bestimmten Knoten, gerichtet auf
	 * diesen Knoten, existiert
	 */
	public boolean incomingVerticesContainsVertex(Vertex vertex) {
		return incoming_vertices.containsKey(vertex);
	}

	/*
	 * Rückggabe der Kante, die von einem bestimmten Knoten kommt und auf den
	 * aktuellen Knoten gerichtet ist.
	 */
	public Edge getIncomingEdge(Vertex vertex) {
		return incoming_vertices.get(vertex);
	}

	/*
	 * Eine Kante, gerichtet auf diesen Knoten ,wird in die zugehörigen Listen
	 * gespeichert. Diese Methode wird und darf nur über die addEdge Methode aus
	 * der DGraph-Klasse aufgerufen werden.
	 */
	void addIncomingEdge(Edge edge) {
		incoming_edges.add(edge);
		incoming_vertices.put(edge.sourceVertex(), edge);
		addAngle(edge);
	}

	/*
	 * Eine Kante gerichtet auf diesen Knoten wird aus den zugehörigen Listen
	 * entfernt. Diese Methode wird und darf nur über die removeEdge Methode aus
	 * der DGraph-Klasse aufgerufen werden.
	 */
	void removeIncomingEdge(Edge edge) {
		incoming_edges.remove(edge);
		incoming_vertices.remove(edge.sourceVertex());
		removeAngle(edge);
	}

	/*
	 * Entfernt den Winkel einer Kante aus den Winkellisten insofern dieser Knoten
	 * nicht imaginär ist.
	 */
	private void removeAngle(Edge edge) {
		if (this instanceof Vertex_right_turn || edge.targetVertex().isImaginary() || edge.sourceVertex().isImaginary())
			return;
		angles_map.remove(edges_angles.remove(edge));
	}

	/*
	 * Berechnet den Winkel einer Kante und fügt diesen in die Listen ein,
	 * insofern der Knoten nicht imaginär ist.
	 */
	private void addAngle(Edge edge) {
		if (this instanceof Vertex_right_turn || edge.targetVertex().isImaginary() || edge.sourceVertex().isImaginary())
			return;
		// Winkelberechnung
		double angle = getAngle(edge);
		// Eintrag in Listen
		angles_map.put(angle, edge);
		edges_angles.put(edge, angle);
	}

	/*
	 * Bestimmt den Winkel einer Kante zu diesem Knoten.
	 */
	private double getAngle(Edge edge) {
		Vertex v1, v2;
		if (edge.sourceVertex() == this) {
			v1 = edge.sourceVertex();
			v2 = edge.targetVertex();
		} else {
			v1 = edge.targetVertex();
			v2 = edge.sourceVertex();
		}
		// Winkelberechnung
		double angle = angleOf(v1.getCoordinates(), v2.getCoordinates());
		return angle;
	}

	/*
	 * Überprüft, ob bereits eine Kante mit dem gleichen Winkel wie eine andere
	 * Kante mit dem Knoten verbunden ist.
	 */
	public boolean IsDuplicateAngle(Edge edge) {
		return angles_map.containsKey(getAngle(edge));
	}

	/*
	 * Gibt die Kanten zurück, die man befahren darf wenn man den Knoten über eine
	 * Kante besucht.
	 */
	public Edge[] getAllowedEdges(Edge edge) throws IllegalArgumentException {

		// Wenn nur eine Kante existiert, kann auch über diese zurückgefahren werden
		if (angles_map.size() == 1)
			return new Edge[] { edge };
		// Rufe Winkel der Kante ab
		Double angleOfEdge = edges_angles.get(edge);
		// Überprüfung, ob Knoten imaginär ist
		if (angleOfEdge == null)
			throw new IllegalArgumentException(
					"Man kann eine Kreuzung nicht über imaginäre Knoten besuchen oder von einer imaginäre Kreuzung kommen!");
		// Kante mit dem nächst größeren Winkel wird ermittelt
		Double next_angle = angles_map.higherKey(angleOfEdge);
		if (next_angle == null)
			next_angle = angles_map.higherKey(new Double(-1));
		// Überprüfung, ob eine gegenüberliegende Kante existiert
		if (angles_map.containsKey((angleOfEdge + 180) % 360)
				&& angles_map.get((angleOfEdge + 180) % 360) != angles_map.get(next_angle))
			return new Edge[] { angles_map.get((angleOfEdge + 180) % 360), angles_map.get(next_angle) };
		return new Edge[] { angles_map.get(next_angle) };

	}

	/*
	 * Gibt alle Kanten zurück, die man nicht befahren darf, wenn man den Knoten
	 * über eine Kante besucht.
	 */
	public Edge[] getProhibitedEdges(Edge visiting_edge) {
		// Wenn nur eine Kante existiert, gibt es keine illegale Kante
		if (angles_map.size() == 1 || angles_map.isEmpty())
			return new Edge[0];
		// Legale Kanten werden abgerufen
		Edge[] nextEdges = getAllowedEdges(visiting_edge);
		Edge[] prohibitedEdges = new Edge[angles_map.size() - nextEdges.length];
		// Ermittle alle Kanten, die nicht legal sind
		int index = 0;
		for (Edge edge : edges_angles.keySet()) {
			if (!(nextEdges[0] == edge || (nextEdges.length > 1 && nextEdges[1] == edge)))
				prohibitedEdges[index++] = edge;
		}
		return prohibitedEdges;
	}

	/*
	 * Gibt den Winkel einer Geraden zurück, die auf den Knoten trifft.
	 */
	private double angleOf(Point pos1, Point pos2) {
		final double deltaY = (pos2.y - pos1.y);
		final double deltaX = (pos2.x - pos1.x);
		// atan2 = [-pi;+pi] -> [-180°;+180°]
		final double result = Math.toDegrees(Math.atan2(deltaY, deltaX));
		return normalizeAngle(result);
	}

	/*
	 * Winkel auf Intervall [0;360) und auf zwei Nachkommastellen normieren.
	 * 
	 */
	private double normalizeAngle(double angle) {
		angle = (double) Math.round(angle * 100) / 100;
		if (angle < 0)
			angle += 360;
		return angle;
	}

	/*
	 * Speichert alle Winkel aller Kanten, die mit dem Knoten verbunden sind.
	 */
	private final TreeMap<Double, Edge> angles_map = new TreeMap<Double, Edge>();
	/*
	 * Speichert alle Kanten und ihren Winkel. (Performance)
	 */
	private final HashMap<Edge, Double> edges_angles = new HashMap<Edge, Double>();

	/*
	 * Gibt eine Liste aller Winkel der Kanten, die auf diesen Knoten treffen,
	 * zurück.
	 */
	public Collection<Double> angles() {
		return angles_map.keySet();
	}

}
