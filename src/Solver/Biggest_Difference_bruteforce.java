package Solver;

import java.awt.Point;
import java.util.HashMap;

public class Biggest_Difference_bruteforce {

	/*
	 * Liefert den gr��ten Unterschied, entstanden durch
	 * das Linksabbiegeverbot und dass
	 * dazugeh�rige Knotenpaar.
	 */
	public static difference_object getBiggestDifference(DGraph_all_directions graph_all, DGraph_right_turn graph_right){
		
		GUI.Frame.algorithm_progress.setValue(0);
		
		//�berpr�fung, ob Unterschied im Graphen �berhaupt existieren kann
		if(graph_all.getVertices().size() < 2){
			GUI.Frame.algorithm_progress.setValue(100);
			difference_object substitute = new difference_object(null,null);
			substitute.setDifference(1);
			return substitute;
		}
		
		//�berpr�fung, ob mindestens zwei Knoten durch das Linksabbiegeverbot nicht mehr erreichbar sind
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
			//Gr��ter Unterschied von diesem Knoten zu einem anderen Knoten wird ermittelt
			difference_object biggest_difference_current_vertex = getBiggestDifferenceSingleVertex(graph_all,graph_right,vertex);
			//Speicherung des bisher gr��ten gefundenen Unterschieds
			if(biggest_difference == null || biggest_difference.compareTo(biggest_difference_current_vertex) == 1)
				biggest_difference = biggest_difference_current_vertex;
			
		}
		GUI.Frame.algorithm_progress.setValue(100);
		return biggest_difference;
		
	}
	
	/*
	 * Sucht den den gr��ten Unterschied,
	 * der von einem ausgew�hlten Knoten
	 * zu einem anderen Knoten des 
	 * Graphen existiert.
	 */
	private static difference_object getBiggestDifferenceSingleVertex(DGraph_all_directions graph_all, DGraph_right_turn graph_right, Vertex source){
		 //Die Entfernung des k�rzesten Weg von "source" Knoten zu allen anderen Knoten des Graphen im Beliebigabbiegegraph wird berechnet
		HashMap<Vertex,dijkstra_object> mapAll = Dijkstra.applyDijkstra(graph_all,source,null,false);
	//Die Entfernung des k�rzesten Weg von "source" Knoten zu allen anderen Knoten des Graphen im Rechtsabbiegegraph wird berechnet
		HashMap<Vertex,dijkstra_object> mapNoLeft = getMapRightProhibition(graph_right,graph_right.getVertex(DGraph_right_turn.generateVertexname(source.getName(),0))); 
		difference_object biggest_difference = null;
		
		//Es wird die Wegl�nge zu jedem Knoten mit und ohne Linksabbiegeverbot berechnet und dividiert
		for(Vertex vertex : graph_all.getVertices()){
			if(vertex == source) continue;		
			
			//Die Wegl�nge im Beliebigabbiegegraph wird abgerufen
			dijkstra_object all_directions = mapAll.get(vertex);
		//F�r jeden �berknoten existiert eine bestimmte Anzahl an Unterknoten im Rechtsabbiegegraph
			//Es wird die Wegl�nge des Unterknotens gew�hlt zu welchem die Wegl�nge am k�rzesten ist
			dijkstra_object no_left = null;
			for(Vertex relative : ((Vertex_right_turn)graph_right.getVertex(DGraph_right_turn.generateVertexname(vertex.getName(),0))).getRelatives()){
				if(no_left == null || mapNoLeft.get(relative).compareTo(no_left) == 1)
					no_left = mapNoLeft.get(relative);
			}
			//Aus beiden Wegl�ngen wird der Unterschied gebildet
			difference_object difference_current_vertex = getDifferenceObjectFromDijkstra(source,vertex,all_directions,no_left);
			//Speicherung des bisher gr��ten gefundenen Unterschieds
			if(biggest_difference == null  || biggest_difference.compareTo(difference_current_vertex) == 1)
				biggest_difference = difference_current_vertex;
		}
		return biggest_difference;
	}
	
	/*
	 * Bildet aus zwei Wegl�ngen den Unterschiedsfaktor
	 */
	private static difference_object getDifferenceObjectFromDijkstra(Vertex source, Vertex sink, dijkstra_object dijkstra_object_source, dijkstra_object dijkstra_object_sink){
		difference_object difference = new difference_object(source,sink);
		//Sind beide Wegl�ngen unendlich so ist der Unterschiedsfaktor 1, weil die Knoten sich nicht erreichen k�nnen, selbst wenn man beliebig abbiegt
		if(dijkstra_object_source.isDistanceInfinite() && dijkstra_object_sink.isDistanceInfinite()){
			difference.setDifference(1);
			return difference;
		}
		//Ist eine der Distanzen Unendlich und die andere nicht, kann das oben erzeugte Objekt zur�ckgegeben werden, da dieses mit unendlich Unterschied initialisiert wurde
		if(dijkstra_object_source.isDistanceInfinite())
			return difference;
		if(dijkstra_object_sink.isDistanceInfinite())
			return difference;
		//Quotient wird in Unterschiedsobjekt errechnet
		difference.setDifference(dijkstra_object_sink.getDistance()/dijkstra_object_source.getDistance());
		return difference;
	}
	

	/*
	 * Gibt eine Liste mit den k�rzesten
	 * Wegen zu allen Knoten im Rechtsabbiegegraph zur�ck
	 */
	private static HashMap<Vertex,dijkstra_object> getMapRightProhibition(DGraph_right_turn graph, Vertex source){
			//Knoten, verbunden mit allen Unterknoten des Startknoten, wird erstellt
			Vertex addedSource = graph.addVertex(new Point(0,0),"#Source");
			for(Vertex vertex : source.getHierarchieEnd()){
				if(!graph.getVertices().contains(vertex)) continue;				
				Edge edge = graph.addEdge(addedSource,vertex);
				edge.setCapacity(0);
			}
			//Erhalte �ber Dijkstra Algorithmus k�rzesten Wegl�ngen zu allen Knoten
			HashMap<Vertex,dijkstra_object> result = Dijkstra.applyDijkstra(graph, addedSource, null, false);
			//Stelle Ursprungszustand wieder her
			graph.removeVertex(addedSource);
			return result;
	}
	
}
