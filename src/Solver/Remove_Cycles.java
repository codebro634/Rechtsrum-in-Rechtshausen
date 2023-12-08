package Solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Remove_Cycles {

	/*
	 * Entfernt alle Zyklen aus einem Graphen.
	 */
	static void removeAllCycles(DGraph graph) {
		HashSet<Vertex> whiteSet = new HashSet<Vertex>(graph.getVertices());
		HashSet<Vertex> blackSet = new HashSet<Vertex>();
		while (removeCycleIteration(graph, whiteSet, blackSet));
	}

	/*
	 * Findet und entfernt einen Zyklus aus einem Graphen. Liefert zurück, ob ein
	 * Zyklus entfernt werden konnte. Knoten, die als nicht zu einem Zyklus
	 * gehörend identifiziert wurden, werden gespeichert. Knoten, die noch nicht
	 * untersucht wurden, werden gespeichert.
	 */
	private static boolean removeCycleIteration(DGraph graph, HashSet<Vertex> stillWhite, HashSet<Vertex> ardyBlack) {
		// Alle unerforschten Knoten
		HashSet<Vertex> whiteSet = stillWhite;
		// Alle Knoten die gerade erforscht werden
		HashSet<Vertex> graySet = new HashSet<Vertex>();
		// Alle Knoten, die nicht zu einem Zyklus gehören
		HashSet<Vertex> blackSet = ardyBlack;
		// Speichert welcher Knoten von welchem Knoten aufgerufen wurde (Für
		// Backtracking)
		HashMap<Vertex, Vertex> parentMap = new HashMap<Vertex, Vertex>();
		// Der Algorithmus läuft solange bis alle Knoten erforscht wurden
		while (whiteSet.size() > 0) {
			// Ein Knoten aus der unerforschten Liste wird gewählt
			Vertex current = whiteSet.iterator().next();
			// Tiefensuche probiert einen Zyklus zu finden
			Vertex cycle_end = dfs(null, current, whiteSet, graySet, blackSet, parentMap);
			// cycle_end ist null, wenn kein Zyklus gefunden wurde
			if (cycle_end != null) {
				// Alle Knoten des Zyklus werden zum Knoten, der als Ende des Zyklus
				// markiert wurde, zusammengefügt
				removeCycleFromGraphWithMap(graph, cycle_end, parentMap);
				// Alle Knoten, die sich noch in der grauen Liste befinden, werden
				// wieder in die weiße Liste verschoben
				for (Vertex gray : new ArrayList<Vertex>(graySet)) {
					if (graph.getVertices().contains(gray))
						moveVertex(gray, graySet, whiteSet);
				}
				// Falls ein Zyklus gefunden wurde wird abgebrochen und dies
				// entsprechend zurückgegeben
				return true;
			}
		}
		// Rückgabe, dass kein Zyklus gefunden wurde
		return false;
	}

	/*
	 * Fasst einen Zyklus mittels Backtracking zusammen.
	 */
	private static void removeCycleFromGraphWithMap(DGraph graph, Vertex cycle_end, HashMap<Vertex, Vertex> parentMap) {
		// Liste der Knoten, die verschmolzen werden.
		ArrayList<Vertex> merge = new ArrayList<Vertex>();

		// Backtracking
		Vertex child = parentMap.get(cycle_end);
		while (child != cycle_end) {
			merge.add(child);
			child = parentMap.get(child);
		}
		// Liste mit den Zyklusknoten wird als Argument an die Methode zum
		// Verschmelzen dieser geliefert
		graph.mergeVertices(cycle_end, merge); // Der Zyklus wird zusammengefasst
	}

	/*
	 * Tiefensuche. Alle gefunden Knoten während der Tiefensuche werden
	 * gespeichert. Findet die Tiefensuche einen bereits aufgerufenen Knoten, muss
	 * ein Zyklus existieren.
	 */
	private static Vertex dfs(Vertex parent, Vertex current, HashSet<Vertex> whiteSet, HashSet<Vertex> graySet,
			HashSet<Vertex> blackSet, HashMap<Vertex, Vertex> parentMap) {
		// Eintrag in die Backtrackingliste
		parentMap.put(current, parent);
		// Knoten wird in die Liste der zu erforschenden Knoten verschoben
		moveVertex(current, whiteSet, graySet);
		for (Edge edge : current.getOutgoingEdges()) {
			// Die Zielknoten aller ausgehenden Kanten werden untersucht
			Vertex neighbor = edge.targetVertex();
			// Falls der gefundene Knoten sich in der schwarzen Liste befindet, wurde
			// dieser bereits erforscht und ist daher uninteressant
			if (blackSet.contains(neighbor)) {
				continue;
			}
			// Falls sich der gefundene Knoten schon in der Liste der zu erforschenden
			// Knoten befindet (graue Liste), heißt es, dass ein Zyklus existiert
			if (graySet.contains(neighbor)) {
				parentMap.put(neighbor, current);
				return neighbor;
			}
			// Hier befindet sich der angrenzende Knoten in der weißen Liste.
			// Die Tiefensuche wird von diesem aus weiter fortgeführt.
			Vertex cycle_end = dfs(current, neighbor, whiteSet, graySet, blackSet, parentMap);
			if (cycle_end != null)
				return cycle_end;
		}
		// An dieser Stelle gehört der untersuchte Knoten nicht zu einem
		// Zyklus und wird daher in die schwarze Liste verschoben
		moveVertex(current, graySet, blackSet);
		return null;
	}

	/*
	 * Verschiebt einen Knoten aus einer Liste in eine andere Liste. Der Knoten
	 * wird aus einer Liste entfernt und einer anderen Liste hinzugefügt.
	 */
	private static void moveVertex(Vertex vertex, HashSet<Vertex> sourceSet, HashSet<Vertex> destinationSet) {
		sourceSet.remove(vertex);
		destinationSet.add(vertex);
	}

}
