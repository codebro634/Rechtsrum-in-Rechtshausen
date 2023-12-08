 package BwInf;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import Solver.DGraph_all_directions;
import Solver.DGraph_right_turn;
import Solver.Edge;
import Solver.Vertex;

public class Import_graph {

	public static final String DIRECTORY = "graphen";
	
	/*
	 * Lässt Nutzer eine Datei auswählen und übergibt die File-Variable
	 * an die Einleseroutine weiter, merkt sich den Dateinamen und übergibt den erzeugten Graph zurück.
	 */
	public static void import_graph() throws IllegalArgumentException{
	//Datei über Benutzereingabe erhalten
		File file = null;
		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir") + "/"+DIRECTORY);
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue != JFileChooser.APPROVE_OPTION) {
			JOptionPane.showMessageDialog(null, "Du hast keinen Graphen ausgewählt.");
			return;
		}

		file = fileChooser.getSelectedFile();
		//Initialisiere Reader und Graphen
		BufferedReader reader = null;

		Control.dgad = new DGraph_all_directions();
		Control.dgrt = new DGraph_right_turn();
		
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			int vertex_amount = -1, edge_amount = -1;
			//Zeilenweise interpretieren
			while (line != null) {
				//Kommentarzeilen ignorieren
				if(line.length() != 0 && line.charAt(0) != '#'){
					//Entferne Sonderzeichen
					line = line.replaceAll("\\p{C}", " ");
					//Mehrfache Leerzeichen zusammenfassen
					line = line.replaceAll("\\s+", " ");
					//Warten auf Knoten- und Kantenzeile
				 if(vertex_amount == -1){
					 vertex_amount = Integer.parseInt(line.split(" ")[0]);
					 edge_amount = Integer.parseInt(line.split(" ")[1]);
					}
				 //Knoten einlesen bis Knotenzahl erreicht
					else{
						if(vertex_amount > 0){
							vertex_amount--;
							String[] split = line.split(" ");
							//Knoten hinzufügen
							Control.dgad.addVertex(new Point((int)Float.parseFloat(split[1]),(int)Float.parseFloat(split[2])),split[0],false);
						}
						//Kanten einlesen bis Kantenanzahl erreicht
						else{
							if(edge_amount > 0 && line.split(" ").length == 2){
								edge_amount--;
								Vertex ver1 = Control.dgad.getVertex(line.split(" ")[0]), ver2 = Control.dgad.getVertex(line.split(" ")[1]);
								Edge edge = new Edge(ver1,ver2);
								//Überprüfung, ob Kante valide ist
								if(ver1.IsDuplicateAngle(edge) || ver2.IsDuplicateAngle(edge)){
									if(reader != null)
										reader.close();
									throw new IllegalArgumentException("Zwei aufeinanderliegende Straßen sind nicht zulässig!");
								}
								else
								//Kante hinzufügen
								Control.dgad.addEdge(ver1, ver2);
							}
						}
					}
				}
				line = reader.readLine();	
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		
		Control.original_graph =Control.dgad.copy();
		Control.original_graph_right = DGraph_right_turn.convert(Control.original_graph);
				
		if(GUI.Frame.show_split_graph.isSelected()){
			GUI.GUI_Graph.displayDGraph(Control.original_graph_right,true);
		}
		else{
			GUI.GUI_Graph.displayDGraph(Control.original_graph, true);
		}

	}
	
}
