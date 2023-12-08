package Solver;

import java.util.ArrayList;

public class DGraph_all_directions extends DGraph{
	
/*
 * Klasse repräsentiert einen Graphen in dem kein Linksabbiegeverbot gilt
 */
	
	/*
	 * Erstellt für jede Kante eine entgegengesetzte Kante
	 */
	public void createInverseEdges(){
		for(Edge edge : new ArrayList<Edge>(getEdges()))
			addEdge(edge.targetVertex(),edge.sourceVertex());
	}
	
}
