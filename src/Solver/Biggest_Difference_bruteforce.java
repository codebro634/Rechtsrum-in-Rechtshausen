package Solver;

import java.awt.Point;
import java.util.HashMap;

public class Biggest_Difference_bruteforce {

	/*
	 * Liefert den größten Unterschied, entstanden durch
	 * das Linksabbiegeverbot und dass
	 * dazugehörige Knotenpaar.
	 */
	public static difference_object getBiggestDifference(DGraph_all_directions graph_all, DGraph_right_turn graph_right){
		
		GUI.Frame.algorithm_progress.setValue(0);
		
		//Überprüfung, ob Unterschied im Graphen überhaupt existieren kann
		if(graph_all.getVertices().size() < 2){
			GUI.Frame.algorithm_progress.setValue(100);
			difference_object substitute = new difference_object(null,null);
			substitute.setDifference(1);
			return substitute;
		}
		
		//Überprüfung, ob mindestens zwei Knoten durch das Linksabbiegeverbot nicht mehr erreichbar sind
		Vertex[] infinite_difference = Vertices_not_reachable.ExistVerticesNotReachableBecauseLeftProhibition(graph_all);
		if(infinite_difference != null){
			GUI.Frame.algorithm_progress.setValue(100);
			return new difference_object(infinite_difference[0],infinite_difference[1]);
		}
		graph_all.createInverseEdges();
		
		int percent = graph_all.getVertices().size()/100;
		if(percent == 0) percent = 1;
		int progress = 0;
		
		//Iterierung aller Knoten des Graphen
		difference_object biggest_difference = null;
		for(Vertex vertex : graph_all.getVertices()){
			if(progress++%percent == 0){
				GUI.Frame.algorithm_progress.setValue(progress/percent);
			}
			//Größter Unterschied von diesem Knoten zu einem anderen Knoten wird ermittelt
			difference_object biggest_difference_current_vertex = getBiggestDifferenceSingleVertex(graph_all,graph_right,vertex);
			//Speicherung des bisher größten gefundenen Unterschieds
			if(biggest_difference == null || biggest_difference.compareTo(biggest_difference_current_vertex) == 1)
				biggest_difference = biggest_difference_current_vertex;
			
		}
		GUI.Frame.algorithm_progress.setValue(100);
		return biggest_difference;
		
	}
	
	/*
	 * Sucht den den größten Unterschied,
	 * der von einem ausgewählten Knoten
	 * zu einem anderen Knoten des 
	 * Graphen existiert.
	 */
	private static difference_object getBiggestDifferenceSingleVertex(DGraph_all_directions graph_all, DGraph_right_turn graph_right, Vertex source){
		 //Die Entfernung des kürzesten Weg von "source" Knoten zu allen anderen Knoten des Graphen im Beliebigabbiegegraph wird berechnet
		HashMap<Vertex,dijkstra_object> mapAll = Dijkstra.applyDijkstra(graph_all,source,null,false);
	//Die Entfernung des kürzesten Weg von "source" Knoten zu allen anderen Knoten des Graphen im Rechtsabbiegegraph wird berechnet
		HashMap<Vertex,dijkstra_object> mapNoLeft = getMapRightProhibition(graph_right,graph_right.getVertex(DGraph_right_turn.generateVertexname(source.getName(),0))); 
		difference_object biggest_difference = null;
		
		//Es wird die Weglänge zu jedem Knoten mit und ohne Linksabbiegeverbot berechnet und dividiert
		for(Vertex vertex : graph_all.getVertices()){
			if(vertex == source) continue;		
			
			//Die Weglänge im Beliebigabbiegegraph wird abgerufen
			dijkstra_object all_directions = mapAll.get(vertex);
		//Für jeden Überknoten existiert eine bestimmte Anzahl an Unterknoten im Rechtsabbiegegraph
			//Es wird die Weglänge des Unterknotens gewählt zu welchem die Weglänge am kürzesten ist
			dijkstra_object no_left = null;
			for(Vertex relative : ((Vertex_right_turn)graph_right.getVertex(DGraph_right_turn.generateVertexname(vertex.getName(),0))).getRelatives()){
				if(no_left == null || mapNoLeft.get(relative).compareTo(no_left) == 1)
					no_left = mapNoLeft.get(relative);
			}
			//Aus beiden Weglängen wird der Unterschied gebildet
			difference_object difference_current_vertex = getDifferenceObjectFromDijkstra(source,vertex,all_directions,no_left);
			//Speicherung des bisher größten gefundenen Unterschieds
			if(biggest_difference == null  || biggest_difference.compareTo(difference_current_vertex) == 1)
				biggest_difference = difference_current_vertex;
		}
		return biggest_difference;
	}
	
	/*
	 * Bildet aus zwei Weglängen den Unterschiedsfaktor
	 */
	private static difference_object getDifferenceObjectFromDijkstra(Vertex source, Vertex sink, dijkstra_object dijkstra_object_source, dijkstra_object dijkstra_object_sink){
		difference_object difference = new difference_object(source,sink);
		//Sind beide Weglängen unendlich so ist der Unterschiedsfaktor 1, weil die Knoten sich nicht erreichen können, selbst wenn man beliebig abbiegt
		if(dijkstra_object_source.isDistanceInfinite() && dijkstra_object_sink.isDistanceInfinite()){
			difference.setDifference(1);
			return difference;
		}
		//Ist eine der Distanzen Unendlich und die andere nicht, kann das oben erzeugte Objekt zurückgegeben werden, da dieses mit unendlich Unterschied initialisiert wurde
		if(dijkstra_object_source.isDistanceInfinite())
			return difference;
		if(dijkstra_object_sink.isDistanceInfinite())
			return difference;
		//Quotient wird in Unterschiedsobjekt errechnet
		difference.setDifference(dijkstra_object_sink.getDistance()/dijkstra_object_source.getDistance());
		return difference;
	}
	

	/*
	 * Gibt eine Liste mit den kürzesten
	 * Wegen zu allen Knoten im Rechtsabbiegegraph zurück
	 */
	private static HashMap<Vertex,dijkstra_object> getMapRightProhibition(DGraph_right_turn graph, Vertex source){
			//Knoten, verbunden mit allen Unterknoten des Startknoten, wird erstellt
			Vertex addedSource = graph.addVertex(new Point(0,0),"#Source");
			for(Vertex vertex : source.getHierarchieEnd()){
				if(!graph.getVertices().contains(vertex)) continue;				
				Edge edge = graph.addEdge(addedSource,vertex);
				edge.setCapacity(0);
			}
			//Erhalte über Dijkstra Algorithmus kürzesten Weglängen zu allen Knoten
			HashMap<Vertex,dijkstra_object> result = Dijkstra.applyDijkstra(graph, addedSource, null, false);
			//Stelle Ursprungszustand wieder her
			graph.removeVertex(addedSource);
			return result;
	}
	
}
