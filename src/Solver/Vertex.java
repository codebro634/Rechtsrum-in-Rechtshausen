package Solver;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.TreeMap;

public class Vertex {

	/*
	 * Ist dieser Knoten imagin�r oder nicht bzw. soll er relevant zur
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
	 * Der Name des Knoten wird zur�ckgegeben.
	 */
	public String getName() {
		return name;
	}

	/*
	 * Die Koordinaten des Knoten werden zur�ckgegeben.
	 */
	public Point getCoordinates() {
		return coordinates;
	}

	/*
	 * Gibt zur�ck, ob der Knoten imagin�r ist.
	 */
	public boolean isImaginary() {
		return imaginary;
	}

	/*
	 * Speichert alle �berknoten aller Knoten, die zu zu diesem Knoten
	 * verschmolzen wurden.
	 */
	private HashSet<Vertex> mergedTops = new HashSet<Vertex>();

	/*
	 * Gibt alle �berknoten zur�ck, die zu diesem Knoten zusammengefasst wurden.
	 */
	public HashSet<Vertex> getMergedTops() {
		return mergedTops;
	}

	/*
	 * F�gt einen �berknoten eines Knotens in die Liste der verschmolzenen
	 * �berknoten ein.
	 */
	void addMergedVertex(Vertex merge) {
		mergedTops.add(merge.getHierarchieTop());
	}

	/*
	 * Gibt den obersten Knoten, der Rangfolge wieder in der sich der Knoten
	 * befindet. Ist der Knoten Teil eines Rechtsabbiegegraph, wird sein
	 * �berknoten zur�ckgegeben. Ist der Knoten Teil eines Beliebigabbiegegraph,
	 * gibt er sich selber zur�ck.
	 */
	public Vertex getHierarchieTop() {
		if (this instanceof Vertex_right_turn && ((Vertex_right_turn) this).getParent() != null)
			return ((Vertex_right_turn) this).getParent();
		return this;
	}

	/*
	 * Gibt die untersten Knoten, der Rangfolge wieder in der sich der Knoten
	 * befindet. Ist der Knoten Teil eines Rechtsabbiegegraph und ein Unterknoten,
	 * gibt er alle Knoten, die denselben �berknoten haben wie er selbst. Ist der
	 * Knoten Teil eines Rechtsabbiegegraphen und ein �berknoten gibt er seine
	 * Unterknoten zur�ck. Ist der Knoten Teil eines Beliebigabbiegegraph gibt er
	 * sich selbst zur�ck.
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
	 * Speichert alle Zielknoten aller ausgehenden Kanten und das dazugeh�rige
	 * Kantenobjekt (Performance)
	 */
	private HashMap<Vertex, Edge> outgoing_vertices = new HashMap<Vertex, Edge>();

	/*
	 * Eine Liste aller ausgehenden Kanten wird zur�ckgegeben.
	 */
	public HashSet<Edge> getOutgoingEdges() {
		return outgoing_edges;
	}

	/*
	 * Es wird zur�ckgegeben, ob dieser Knoten eine Kante besitzt, die auf einen
	 * bestimmten Knoten gerichtet ist.
	 */
	public boolean outgoingVerticesContainsVertex(Vertex vertex) {
		return outgoing_vertices.containsKey(vertex);
	}

	/*
	 * R�ckggabe der Kante kommend von diesem Knoten und gerichtet auf einen
	 * bestimmten Knoten falls vorhanden
	 */
	public Edge getOutgoingEdge(Vertex vertex) {
		return outgoing_vertices.get(vertex);
	}

	/*
	 * Eine Kante, die diesen Knoten als Quellknoten hat, wird in die zugeh�rigen
	 * Listen eingetragen. Diese Methode wird und darf nur �ber die addEdge
	 * Methode aus der DGraph-Klasse aufgerufen werden.
	 */
	void addOutgoingEdge(Edge edge) {
		outgoing_edges.add(edge);
		outgoing_vertices.put(edge.targetVertex(), edge);
		addAngle(edge);
	}

	/*
	 * Entfernt eine Kante, die diesen Knoten als Quellknoten hat, aus den
	 * zuge�rigen Listen. Diese Methode wird darf nur �ber die Methode removeEdge
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
	 * Speichert alle Quellknoten aller eingehenden Kanten und das dazugeh�rige
	 * Kantenobjekt (Performance)
	 */
	private HashMap<Vertex, Edge> incoming_vertices = new HashMap<Vertex, Edge>();

	/*
	 * Liste aller eingehenden Kanten wird zur�ckgegeben.
	 */
	public HashSet<Edge> getIncomingEdges() {
		return incoming_edges;
	}

	/*
	 * R�ckggabe, ob eine Kante kommend von einem bestimmten Knoten, gerichtet auf
	 * diesen Knoten, existiert
	 */
	public boolean incomingVerticesContainsVertex(Vertex vertex) {
		return incoming_vertices.containsKey(vertex);
	}

	/*
	 * R�ckggabe der Kante, die von einem bestimmten Knoten kommt und auf den
	 * aktuellen Knoten gerichtet ist.
	 */
	public Edge getIncomingEdge(Vertex vertex) {
		return incoming_vertices.get(vertex);
	}

	/*
	 * Eine Kante, gerichtet auf diesen Knoten ,wird in die zugeh�rigen Listen
	 * gespeichert. Diese Methode wird und darf nur �ber die addEdge Methode aus
	 * der DGraph-Klasse aufgerufen werden.
	 */
	void addIncomingEdge(Edge edge) {
		incoming_edges.add(edge);
		incoming_vertices.put(edge.sourceVertex(), edge);
		addAngle(edge);
	}

	/*
	 * Eine Kante gerichtet auf diesen Knoten wird aus den zugeh�rigen Listen
	 * entfernt. Diese Methode wird und darf nur �ber die removeEdge Methode aus
	 * der DGraph-Klasse aufgerufen werden.
	 */
	void removeIncomingEdge(Edge edge) {
		incoming_edges.remove(edge);
		incoming_vertices.remove(edge.sourceVertex());
		removeAngle(edge);
	}

	/*
	 * Entfernt den Winkel einer Kante aus den Winkellisten insofern dieser Knoten
	 * nicht imagin�r ist.
	 */
	private void removeAngle(Edge edge) {
		if (this instanceof Vertex_right_turn || edge.targetVertex().isImaginary() || edge.sourceVertex().isImaginary())
			return;
		angles_map.remove(edges_angles.remove(edge));
	}

	/*
	 * Berechnet den Winkel einer Kante und f�gt diesen in die Listen ein,
	 * insofern der Knoten nicht imagin�r ist.
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
	 * �berpr�ft, ob bereits eine Kante mit dem gleichen Winkel wie eine andere
	 * Kante mit dem Knoten verbunden ist.
	 */
	public boolean IsDuplicateAngle(Edge edge) {
		return angles_map.containsKey(getAngle(edge));
	}

	/*
	 * Gibt die Kanten zur�ck, die man befahren darf wenn man den Knoten �ber eine
	 * Kante besucht.
	 */
	public Edge[] getAllowedEdges(Edge edge) throws IllegalArgumentException {

		// Wenn nur eine Kante existiert, kann auch �ber diese zur�ckgefahren werden
		if (angles_map.size() == 1)
			return new Edge[] { edge };
		// Rufe Winkel der Kante ab
		Double angleOfEdge = edges_angles.get(edge);
		// �berpr�fung, ob Knoten imagin�r ist
		if (angleOfEdge == null)
			throw new IllegalArgumentException(
					"Man kann eine Kreuzung nicht �ber imagin�re Knoten besuchen oder von einer imagin�re Kreuzung kommen!");
		// Kante mit dem n�chst gr��eren Winkel wird ermittelt
		Double next_angle = angles_map.higherKey(angleOfEdge);
		if (next_angle == null)
			next_angle = angles_map.higherKey(new Double(-1));
		// �berpr�fung, ob eine gegen�berliegende Kante existiert
		if (angles_map.containsKey((angleOfEdge + 180) % 360)
				&& angles_map.get((angleOfEdge + 180) % 360) != angles_map.get(next_angle))
			return new Edge[] { angles_map.get((angleOfEdge + 180) % 360), angles_map.get(next_angle) };
		return new Edge[] { angles_map.get(next_angle) };

	}

	/*
	 * Gibt alle Kanten zur�ck, die man nicht befahren darf, wenn man den Knoten
	 * �ber eine Kante besucht.
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
	 * Gibt den Winkel einer Geraden zur�ck, die auf den Knoten trifft.
	 */
	private double angleOf(Point pos1, Point pos2) {
		final double deltaY = (pos2.y - pos1.y);
		final double deltaX = (pos2.x - pos1.x);
		// atan2 = [-pi;+pi] -> [-180�;+180�]
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
	 * zur�ck.
	 */
	public Collection<Double> angles() {
		return angles_map.keySet();
	}

}
