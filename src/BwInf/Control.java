package BwInf;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JOptionPane;

import GUI.Frame;
import GUI.GUI_Graph;
import Solver.*;

public class Control {
	// Zu bearbeitender Beliebigabbiegegraph
	public static DGraph_all_directions dgad;
	// Zu bearbeitender Rechtsabbiegegraph
	public static DGraph_right_turn dgrt;

	public static DGraph_all_directions original_graph;
	public static DGraph_right_turn original_graph_right;

	public static boolean consider_all_directions_graph = false;

	/*
	 * Hauptmethode
	 */
	public static void main(String[] args) {
		Frame.frame_setup();
	}

	/*
	 * Gibt entweder den geladenen Beliebigabbiegegraph zurück oder den
	 * Rechtsabbiegegraph, je nach dem was im GUI ausgewählt wurde.
	 */
	public static DGraph getSelectedGraph() {
		DGraph graph;
		if (consider_all_directions_graph)
			graph = dgad;
		else
			graph = dgrt;
		if (graph == null || graph.getVertices().size() == 0)
			return null;
		return graph;
	}

	public static final byte TASK_PATH = 0;
	public static final byte TASK_PATH_ILLEGALTURNS = 1;
	public static final byte TASK_ALL_REACHABLE = 2;
	public static final byte TASK_BIGGEST_CHANGE_HEU = 3;
	public static final byte TASK_BIGGEST_CHANGE_BF = 4;

	static byte task = TASK_PATH;

	/*
	 * Setzen, welche Aufgabe mit 'Löse Aufgabe' gelöst werden soll.
	 */
	public static void setTask(byte task) {
		Control.task = task;
	}

	public static Thread algorithm_thread;

	/*
	 * Startet die über das GUI gewählte Aufgabe.
	 */
	public static void solve() {
		if (original_graph == null)
			return;
		algorithm_thread = new Thread(new Solve_thread());
		algorithm_thread.start();
	}

	/*
	 * Aufgabenteil 5) - Größter Unterschied. Rahmenbedingungen werden überprüft,
	 * dann wird die Lösungsmethode aufgerufen und die Lösung im GUI ausgegeben.
	 * Eingabe ob das Ergebnis mit Brute-Force oder heuristisch bestimmt werden
	 * soll.
	 */
	static void biggest_impact(boolean heuristic) {

		Solver.difference_object big = null;

		if (heuristic)
			// Aufruf der Lösungsmethode
			big = Biggest_Difference_heuristic.getBiggestDifference(Control.dgad, Control.dgrt);
		else
			big = Biggest_Difference_bruteforce.getBiggestDifference(Control.dgad, Control.dgrt);

		String solution = "";

		if (big.noDifference())
			solution = "Durch das Rechtsfahrverbot ergibt sich kein Unterschied";
		else if (big.isDifferenceInfinite()) {
			solution = "Durch das Rechtsfahrverbot sind mindestens zwei Knoten nicht mehr erreichbar.\n";
			solution += "Von " + big.getFrom().getName() + " zu " + big.getTo().getName();
		} else {
			if (GUI.GUI_Graph.capacity_is_distance)
				solution += "Metrik: Entfernung\n";
			else
				solution += "Metrik: Knotenanzahl\n";
			solution += "Weglänge um den Faktor\n";
			System.out.println(big.getFrom().getName() + " " + big.getTo().getName());
			try {
				solution += big.getDifference() + " erhöht.\n";
			} catch (Exception e) {
				e.printStackTrace();
			}
			solution += big.getFrom().getName() + " -> " + big.getTo().getName();
			GUI.Frame.sink_vertex.setText(big.getTo().getName());
			GUI.Frame.source_vertex.setText(big.getFrom().getName());

		}
		GUI.Frame.solution_text_field.setText(solution);
	}

	/*
	 * Aufgabenteil 4) - Alle Knoten gegenseitig erreichbar. Rahmenbedingungen
	 * werden überprüft dann wird die Lösungsmethode aufgerufen und die Lösung im
	 * GUI ausgegeben.
	 */
	static void all_vertices_reachable() {
		// Aufruf der Lösungsmethode
		Stack<Vertex[]> vertices = Solver.Vertices_not_reachable.VerticesWhichCantReachEachOther(getSelectedGraph(), false);
		String solution = "";

		if (vertices.isEmpty()) {
			solution = "Alle Knoten können sich gegenseitig erreichen";
		} else {
			solution = "Nicht alle Knoten können sich gegenseitig erreichen.\n";
			solution += "Beispiel für Knoten, die sich gegenseitig nicht erreichen können.\n";
			solution += "x -> y = Knoten y kann von Knoten x aus nicht erreicht werden.\n";
			solution += vertices.peek()[0].getHierarchieTop().getName() + " -> "
					+ vertices.peek()[1].getHierarchieTop().getName();
		}

		GUI.Frame.solution_text_field.setText(solution);
	}

	/*
	 * Aufgabenteil 3) - Kürzester Weg. Rahmenbedingungen werden überprüft dann
	 * wird die Lösungsmethode aufgerufen und die Lösung im GUI ausgegeben.
	 */
	static void shortest_path() {
		String vertex_source = GUI.Frame.source_vertex.getText();
		String vertex_target = GUI.Frame.sink_vertex.getText();

		if (vertex_source.equals(vertex_target)) {
			JOptionPane.showMessageDialog(null, "Keine Navigation erforderlich, da Start=Ziel");
			return;
		}

		DGraph graph = getSelectedGraph();

		if (graph == null) {
			JOptionPane.showMessageDialog(null,
					"Es wurde kein Graph geladen bei dem ein kürzester Weg gefunden werden kann!");
			return;
		}

		Vertex source, target;
		if (graph instanceof DGraph_right_turn) {
			source = graph.getVertex(DGraph_right_turn.generateVertexname(vertex_source, 0));
			target = graph.getVertex(DGraph_right_turn.generateVertexname(vertex_target, 0));
		} else {
			source = graph.getVertex(vertex_source);
			target = graph.getVertex(vertex_target);
		}

		if (source == null || target == null) {
			JOptionPane.showMessageDialog(null, "Einer der ausgewählten Knoten ist nicht im Graphen vorhanden!");
			return;
		}
		// Aufruf der Lösungsmethode
		ArrayList<Vertex> path = Shortest_Path.shortest_path(graph, source, target);

		String solution = "";
		if (path == null)
			solution = "Es existiert kein Weg von " + vertex_source + " zu " + vertex_target;
		else {
			solution = evaluatePath(path, false);
		}

		GUI.Frame.solution_text_field.setText(solution);

	}

	/*
	 * Inhaltliche Erweiterung - Kürzester Weg mit Linksabbiegen.
	 * Rahmenbedingungen werden überprüft dann wird die Lösungsmethode aufgerufen
	 * und die Lösung im GUI ausgegeben.
	 */
	public static void shortest_path_illegalturns() {
		String vertex_source = GUI.Frame.source_vertex.getText();
		String vertex_target = GUI.Frame.sink_vertex.getText();

		if (vertex_source.equals(vertex_target)) {
			JOptionPane.showMessageDialog(null, "Keine Navigation erforderlich, da Start=Ziel");
			return;
		}

		if (Control.dgad == null) {
			JOptionPane.showMessageDialog(null,
					"Es wurde kein Graph geladen bei dem ein kürzester Weg gefunden werden kann!");
			return;
		}

		if (getSelectedGraph() instanceof DGraph_all_directions) {
			JOptionPane.showMessageDialog(null,
					"Aufgabe nicht ausführbar. Wieso sollte man erlauben falsch abbiegen zu dürfen, wenn das Verbot gar nicht existiert?");
			return;
		}

		Vertex source, target;

		source = Control.dgrt.getVertex(DGraph_right_turn.generateVertexname(vertex_source, 0));
		target = Control.dgrt.getVertex(DGraph_right_turn.generateVertexname(vertex_target, 0));

		if (source == null || target == null) {
			JOptionPane.showMessageDialog(null, "Einer der ausgewählten Knoten ist nicht im Graphen vorhanden!");
			return;
		}

		int illegal_turns;

		try {
			illegal_turns = Integer.parseInt(JOptionPane.showInputDialog("Anzahl erlaubten Linksabbiegens."));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Es wurde keine Anzahl an erlaubten Vergehen angegeben!");
			return;
		}

		if (illegal_turns < 1) {
			JOptionPane.showMessageDialog(null, "Mindestanzahl von 1 ist erfordert.");
			return;
		}
		// Aufruf der Lösungsmethode
		ArrayList<Vertex> path = Shortest_Path_illegal_turns.getPath(Control.dgad, Control.dgrt, source, target,
				illegal_turns);

		String solution = "";
		if (path == null)
			solution = "Es existiert kein Weg von " + vertex_source + " zu " + vertex_target;
		else {
			solution = evaluatePath(path, true);
		}

		GUI.Frame.solution_text_field.setText(solution);

	}

	/*
	 * Ein Pfad in Form einer Liste wird ausgewertet. D.h Kanten und Knoten werden
	 * markiert und die gesamte Lösung wird ein Form eines Strings wiedergegeben.
	 */
	private static String evaluatePath(ArrayList<Vertex> path, boolean illegal_edges_contained) {
		if (GUI.Frame.show_split_graph.isSelected()) {
			GUI.Frame.show_split_graph.setSelected(false);
			GUI_Graph.displayDGraph(BwInf.Control.original_graph, false);
		}

		String solution = "";
		try {
			solution = path.get(path.size() - 1).getHierarchieTop().getName() + " -> "
					+ path.get(0).getHierarchieTop().getName() + "\n";

			float length = 0;
			for (int i = 0; i < path.size() - 1; i++) {
				length += path.get(i).getIncomingEdge(path.get(i + 1)).getCapacity();
			}

			if (GUI.GUI_Graph.capacity_is_distance)
				solution += "Metrik: Entfernung\n";
			else
				solution += "Metrik: Knotenanzahl\n";

			if (GUI.GUI_Graph.capacity_is_distance)
				solution += "Weglänge: " + length + " Einheiten";
			else
				solution += "Weglänge: " + length + " Knoten";
			solution += "\n";

			for (int i = path.size() - 1; i >= 0; i--) {
				solution += "\n" + path.get(i).getHierarchieTop().getName();
				GUI.GUI_Graph.markVertex(path.get(i).getHierarchieTop().getName(), Color.GREEN);
				if (i != path.size() - 1) {
					if (illegal_edges_contained && HashtagCount(path.get(i).getName()) != HashtagCount(path.get(i + 1).getName()))
						GUI.GUI_Graph.markEdge(path.get(i).getHierarchieTop().getName(),
								path.get(i + 1).getHierarchieTop().getName(), Color.RED);
					else
						GUI.GUI_Graph.markEdge(path.get(i).getHierarchieTop().getName(),
								path.get(i + 1).getHierarchieTop().getName(), Color.GREEN);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return solution;
	}

	/*
	 * Zählt wie viele # am Anfang eines String sind. Wird benötigt, um um falsch
	 * abgebogen Kanten zu markieren.
	 */
	private static int HashtagCount(String name) {
		int i = -1;
		while (name.charAt(++i) == '#')
			;
		return i;
	}

	public static boolean thread_running = false;

}

class Solve_thread implements Runnable {

	private static final String IN_PROGRESS = "Aufgabe wird\nausgeführt...";

	/*
	 * Methode löst die aktuell ausgewählte Methode bei dem aktuell ausgewählten
	 * Graph.
	 */
	public void run() {
		// Kopien vom Originalgraphen werden erstellt, auf welche die Verfahren
		// angewendet werden
		Control.dgad = Control.original_graph.copy();
		GUI.Frame.algorithm_progress.setValue(0);
		GUI.Frame.solution_text_field.setText(IN_PROGRESS);
		Control.dgrt = Solver.DGraph_right_turn.convert(Control.dgad);
		// Falls, das Verfahren es nicht selber übernimmt, wird der
		// Beliebigabbiegraph erstellt, indem Rückkanten hinzugefügt werden
		if (Control.task != Control.TASK_PATH_ILLEGALTURNS && Control.task != Control.TASK_BIGGEST_CHANGE_BF
				&& Control.task != Control.TASK_BIGGEST_CHANGE_HEU)
			Control.dgad.createInverseEdges();

		DGraph graph = Control.getSelectedGraph();
		if (graph == null || graph.getVertices().size() == 0) {
			JOptionPane.showMessageDialog(null, "Es wurde noch kein Rechtshausen geladen.");
			return;
		}

		Control.thread_running = true;

		GUI.GUI_Graph.clearMarkedEdges();
		GUI.GUI_Graph.clearMarkedVertices();

		long start_time = System.currentTimeMillis();

		// Aufruf der ausgewählten Lösungsverfahren
		switch (Control.task) {
		case Control.TASK_PATH:
			Control.shortest_path();
			break;
		case Control.TASK_PATH_ILLEGALTURNS:
			Control.shortest_path_illegalturns();
			break;
		case Control.TASK_ALL_REACHABLE:
			Control.all_vertices_reachable();
			break;
		case Control.TASK_BIGGEST_CHANGE_HEU:
			Control.biggest_impact(true);
			break;
		case Control.TASK_BIGGEST_CHANGE_BF:
			Control.biggest_impact(false);
			break;
		}

		System.out.println("Laufzeit:" + (System.currentTimeMillis() - start_time) + "ms");

		GUI.GUI_Graph.paint();
		Control.thread_running = false;
		if (GUI.Frame.solution_text_field.getText().equals(IN_PROGRESS))
			GUI.Frame.solution_text_field.setText("");
	}

}
