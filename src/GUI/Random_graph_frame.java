package GUI;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import BwInf.MapGenerator;

public class Random_graph_frame extends JFrame implements ActionListener{

	/*
	 * Klasse, um das Fenster beim 
	 * Auswählen der Parameter eines zufälligen
	 * Graphen anzuzeigen.
	 */
	
	private static final long serialVersionUID = 1L;
	
	public static void displayFrame(){
		new Random_graph_frame();
	}
	
	/*
	 * Erzeugen des Fensters
	 */
	private Random_graph_frame(){
		setSize(500, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setLayout(null);
		setResizable(true);
		setTitle("Zufälliges Straßennetz");
		setVisible(true);
		setup();
	}
	
	private static JTextArea vertex_count = new JTextArea();
	private static JTextArea edge_count = new JTextArea();
	private static JTextArea seed = new JTextArea();
	private static JTextArea xrange = new JTextArea();
	private static JTextArea yrange = new JTextArea();
	
	/*
	 * Füge dem Fenster alle Elemente hinzu
	 */
	private void setup(){
		int current_row = 1;
		createText(current_row,"Seed",50,100,50);
		createTextField(current_row++, seed,"seed",280,25,170);
		createText(current_row,"Knotenanzahl",50,100,50);
		createTextField(current_row++, vertex_count,"knotenanzahl",280,25,170);
		createText(current_row,"Kantenanzahl",50,100,50);
		createTextField(current_row++,edge_count,"kantenanzahl",280,25,170);
		createText(current_row,"X-Range",50,100,50);
		createTextField(current_row++, xrange,"xrange",280,25,170);
		createText(current_row,"Y-Range",50,100,50);
		createTextField(current_row++, yrange,"yrange",280,25,170);
		createActionButton(6,new JButton(),"Fertig",50,35,400);
	}
	
	private int y(int row){
		return (row)*60;
	}
	
	private void createTextField(int row, JTextArea text_field,String title, int width, int height, int x){
		text_field.setEditable(true);
		text_field.setBounds(x, y(row), width, height);
		add(text_field);
	}

	private void createText(int row, String text, int x, int width, int height){
		Font font = new Font("Arial", Font.BOLD, 12);
		JLabel tl1 = new JLabel(text);	
		tl1.setFont(font);		
		tl1.setBounds(x,y(row)-10,width,height);
		add(tl1);
	}
	
	private void createActionButton(int current_row, JButton button, String title, int x, int height, int width){
		button.setText(title);
		button.setActionCommand(title);
		if(button.getActionListeners().length == 0)
		button.addActionListener(this);
		button.setBounds(x,y(current_row)+30,width,height);
		add(button);
	}

	public void actionPerformed(ActionEvent arg0) {
		try{
			int vertices = Integer.parseInt(vertex_count.getText());
			int edges = Integer.parseInt(edge_count.getText());
			long seed_ = Long.parseLong(seed.getText());
			int x_range = Integer.parseInt(xrange.getText());
			int y_range = Integer.parseInt(yrange.getText());
			MapGenerator.generateMap(x_range, y_range, vertices, edges, seed_);
			dispose();
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null, "Überprüfen sie bitte die Eingaben!");
		}
	}
	
}
