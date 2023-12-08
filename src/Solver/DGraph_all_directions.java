package Solver;

import java.util.ArrayList;

public class DGraph_all_directions extends DGraph{
	
/*
 * Klasse repr�sentiert einen Graphen in dem kein Linksabbiegeverbot gilt
 */
	
	/*
	 * Erstellt f�r jede Kante eine entgegengesetzte Kante
	 */
	public void createInverseEdges(){
		for(Edge edge : new ArrayList<Edge>(getEdges()))
			addEdge(edge.targetVertex(),edge.sourceVertex());
	}
	
}
