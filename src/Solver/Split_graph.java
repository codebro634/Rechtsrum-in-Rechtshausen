package Solver;

import java.util.HashSet;
import java.util.Stack;

public class Split_graph {

	/*
	 * Teilt einen Graphen in einzelne zusammenhängende Graphen auf.
	 */
	public static Stack<DGraph_all_directions> splitGraph(DGraph_all_directions graph) {
		Stack<DGraph_all_directions> splitGraphs = new Stack<DGraph_all_directions>();
		HashSet<Vertex> unexplored_vertices = new HashSet<Vertex>(graph.getVertices());
		while (!unexplored_vertices.isEmpty()) {
			Vertex nextVertex = unexplored_vertices.iterator().next();
			// Bestimme Knoten, die mit ausgewähltem Knoten verbunden sind
			HashSet<Vertex> connectedVertices = getVerticesConnectedToVertex(nextVertex);
			unexplored_vertices.removeAll(connectedVertices);
			// Bilde Graph aus zusammenhängenden Knoten
			DGraph_all_directions splitGraph = new DGraph_all_directions();
			for (Vertex vertex : connectedVertices)
				splitGraph.addVertex(vertex.getCoordinates(), vertex.getName(), false);
			for (Vertex vertex : connectedVertices) {
				for (Edge edge : vertex.getOutgoingEdges())
					splitGraph.addEdge(splitGraph.getVertex(edge.sourceVertex().getName()),
							splitGraph.getVertex(edge.targetVertex().getName()));
			}
			splitGraphs.push(splitGraph);
		}
		return splitGraphs;
	}

	/*
	 * Liefert alle Knoten, die mit einem Knoten zusammenhängen. Optional werden
	 * nur die Knoten geliefert, die auch erreichbar sind. Dies ist mittels
	 * Breitensuche umgesetzt.
	 */
	private static HashSet<Vertex> getVerticesConnectedToVertex(Vertex source) {
		HashSet<Vertex> visited_vertices = new HashSet<Vertex>();
		HashSet<Vertex> iteration_list = new HashSet<Vertex>();
		visited_vertices.add(source);
		iteration_list.add(source);
		Stack<Vertex> in_queue = new Stack<Vertex>();
		// Breitensuche
		while (!iteration_list.isEmpty()) {
			for (Vertex vertex : iteration_list) {		
				for (Edge edge : vertex.getIncomingEdges()) {
						if (visited_vertices.contains(edge.sourceVertex()))
							continue;
						visited_vertices.add(edge.sourceVertex());
						in_queue.push(edge.sourceVertex());
					}
				for (Edge edge : vertex.getOutgoingEdges()) {
					if (visited_vertices.contains(edge.targetVertex()))
						continue;
					visited_vertices.add(edge.targetVertex());
					in_queue.push(edge.targetVertex());
				}
			}
			iteration_list.clear();
			iteration_list.addAll(in_queue);
			in_queue.clear();
		}
		return visited_vertices;
	}

}
