package GUI;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import BwInf.Control;

public class Frame extends JFrame implements ActionListener, ChangeListener{

	/*
	 * Klasse erstellt und operiert das Hauptfenster
	 */
	
	private static final long serialVersionUID = 5897208149835730419L;

	private static final int WIDTH_OF_RIGHT_AREA = 200, WIDTH_OF_LEFT_AREA = 200, INITIAL_FRAME_WIDTH = 1200, INITIAL_FRAME_HEIGHT = 800;
	
	private static int OFFSET_RIGHTAREA_X, OFFSET_RIGHTAREA_Y;
	private static int OFFSET_DRAWAREA_X, OFFSET_DRAWAREA_Y, DRAWAREA_WIDTH, DRAWAREA_HEIGHT;
	
	private static int CURRENT_TOTAL_WIDTH, CURRENT_TOTAL_HEIGHT;
	
	public static void frame_setup(){
		new Frame().redrawFrame();
	}
	
	/*
	 * Aufbau des Fensters. Methoden sind dazu um,
	 * um Elemente dem Fenster hinzuzufügen.
	 */
	private Frame(){
		setSize(INITIAL_FRAME_WIDTH, INITIAL_FRAME_HEIGHT);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		setResizable(true);
		setTitle("BwInf Runde 2 - Aufgabe 2 - Rechtsrum in Rechthausen");
		setVisible(true);
		addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent e) {
       redrawFrame();
			}
			public void componentHidden(ComponentEvent e) {}
			public void componentMoved(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {}
			
		});
		redrawFrame();
		
		GUI_Graph.GRAPHICS.addMouseMotionListener(GUI_Graph.events);
		GUI_Graph.GRAPHICS.addMouseWheelListener(GUI_Graph.events);
	}
	
	public void redrawFrame(){
		calculate_gui_settings();
		
		getContentPane().removeAll();
		
		createLeftArea();
		createRightArea();
		createDrawArea();

		revalidate();
		repaint();
	}

	private void calculate_gui_settings() {
		CURRENT_TOTAL_WIDTH = getWidth();
		CURRENT_TOTAL_HEIGHT = getHeight();
		
		OFFSET_DRAWAREA_X = WIDTH_OF_LEFT_AREA;
		DRAWAREA_WIDTH = CURRENT_TOTAL_WIDTH-(WIDTH_OF_LEFT_AREA+WIDTH_OF_RIGHT_AREA);
		OFFSET_DRAWAREA_Y = 0;
		DRAWAREA_HEIGHT = CURRENT_TOTAL_HEIGHT-getInsets().top-getInsets().bottom;
		
		OFFSET_RIGHTAREA_X = WIDTH_OF_LEFT_AREA+DRAWAREA_WIDTH;
		OFFSET_RIGHTAREA_Y = 0;
	}
	
	private static final int ROW_DISTANCE = 25;

	/*
	 * Hauptkomponenten des Fensters
	 */
	
	public static JTextArea solution_text_field = new JTextArea();
	
	public static JTextArea source_vertex = new JTextArea();

	public static JTextArea sink_vertex = new JTextArea();
	
	public static JProgressBar algorithm_progress = new JProgressBar();
	
	public static JCheckBox show_split_graph = new JCheckBox();
	
	private static JScrollPane solution_text_pane;
	
	private static JRadioButton distance = new JRadioButton(),vertex_count = new JRadioButton();
	
	private static JRadioButton only_right = new JRadioButton(), all = new JRadioButton();
	
	private static JRadioButton shortest_path = new JRadioButton(), shortest_path_prohibitedturns = new JRadioButton(), all_reachable = new JRadioButton(), biggest_difference_heu = new JRadioButton(), biggest_difference_bf = new JRadioButton();
	
	/*
	 * Default Belegung 
	 */
	static{
		shortest_path.setSelected(true);
		distance.setSelected(true);
		only_right.setSelected(true);
	}
	
	/*
	 * Fülle linke Seite
	 * des Fensters mit Elementen
	 */
	private void createLeftArea() {
		int current_row = 1;
		createActionButton(false,current_row++,new JButton(),"Zufälliger Graph");
		current_row++;
		createActionButton(false,current_row++,new JButton(),"Importiere Graph");
		current_row++;
		
		createText(false, current_row++, "Metrik");
		String[] titles = {"Entfernung","Knotenanzahl"};
		JRadioButton[] metric = {distance,vertex_count};
		createRadioButtons(false,current_row++,metric,titles);
		current_row+=2;
		createText(false,current_row++,"Abbiegeregeln");
		String[] titles2 = {"Beliebig Abbiegen", "Rechts Abbiegen"};
		JRadioButton[] rules = {all,only_right};
		createRadioButtons(false,current_row++,rules,titles2);
		current_row+=2;
		
		createText(false, current_row++, "        Startknoten      Zielknoten");
		createTextField(false, current_row,source_vertex,null,true,false,"Startknoten",70,20,20);
		createTextField(false, current_row++, sink_vertex,null,true,false,"Zielknoten",70,20,110);
		
		String[] titles3 = {"3.1) Kürzester Weg","3.2) Kürzester Weg (Illegal)","4) Alle Knoten erreichbar","5) Größter Unterschied (HEU)","5) Größter Unterschied (BF)"};
		JRadioButton[] task = {shortest_path,shortest_path_prohibitedturns,all_reachable,biggest_difference_heu,biggest_difference_bf};
		createRadioButtons(false,current_row++, task, titles3);
		current_row+=5;
		
		createActionButton(false, current_row++, new JButton(), "Löse Aufgabe");
		createTextField(false, ++current_row,solution_text_field,solution_text_pane,false,true,"Ausgabefeld",WIDTH_OF_LEFT_AREA,DRAWAREA_HEIGHT-row_y(current_row)-5,0);
	}
	
	
	private static JCheckBox edge_capacities = new JCheckBox();
	
	private static JCheckBox vertex_names = new JCheckBox();
	
	private static JCheckBox edge_angles = new JCheckBox();
	
	private static JSlider vertex_density = new JSlider();
	
	private static JSlider zoom_speed = new JSlider();
	
	private static JSlider slide_speed = new JSlider();
	
	/*
	 * Fülle rechte Seite des Fenters
	 * mit Elementen
	 */
	private void createRightArea() {
		int current_row = 1;
		
		createCheckbox(true,current_row++,show_split_graph,"Aufgeteilter Graph");
		createCheckbox(true,current_row++,edge_angles,"Kantenwinkel zeigen");
		createCheckbox(true,current_row++,vertex_names,"Knotennamen zeigen");
		vertex_names.setSelected(GUI_Graph.isVertexNameDisplayed());
		createCheckbox(true, current_row++,edge_capacities,"Kantenkapazitäten zeigen");
		
		createText(true,current_row++,"Knotenabstand");
		createSlider(true,current_row++,vertex_density,"Knotendichte",0,1000);
		createText(true,current_row++,"Zoomtempo");
		createSlider(true,current_row++,zoom_speed,"Zoom-Tempo",0,100);
		createText(true,current_row++,"Slidetempo");
		createSlider(true,current_row++,slide_speed,"Slide-Tempo",0,100);
		GUI.GUI_Graph.setScrollSpeed(50);
		GUI.GUI_Graph.setZoomSpeed(50);
		current_row++;
		createActionButton(true,current_row++,new JButton(),"Gehe zu Knoten");
		current_row++;
		createActionButton(true,current_row++,new JButton(),"Zentrieren und Skalieren");
		current_row++;
		createText(true,current_row++,"Fortschritt");
		createProgressBar(true, current_row++,algorithm_progress,"Fortschritt");
		current_row++;
		createActionButton(true,current_row++, new JButton(), "Abbrechen");
		current_row++;
	}
	
	/*
	 * Folgende Methoden fügen dem Fenster
	 * Elemente hinzu
	 */
	private void createProgressBar(boolean left_side, int row, JProgressBar bar, String title){
		setBoundOfComponent(bar,left_side,row,150,30);
		bar.setStringPainted(true);
		bar.setMinimum(0);
		bar.setMaximum(100);
		add(bar);
	}
	
	private void createText(boolean left_side, int row, String text){
		Font font = new Font("Arial", Font.BOLD, 12);
		JLabel tl1 = new JLabel(text);	
		tl1.setFont(font);		
		setBoundOfComponent(tl1, left_side, row, 200, 20);	
		add(tl1);
	}
	
	private void createTextField(boolean left_side, int row, JTextArea text_field, JScrollPane scroll, boolean editable, boolean scrollable, String title, int width, int height, int x){
		text_field.setEditable(editable);
		if(scrollable){
    scroll = new JScrollPane(text_field);
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    setBoundOfComponent(scroll,left_side,row,width,height,x);
		add(scroll);
		}
		else{
			setBoundOfComponent(text_field,left_side,row,width,height,x);
			add(text_field);
		}
	}
	
	private void createSlider(boolean left_side, int row, JSlider slider, String title, int from, int to){
		slider.setMinimum(from);
		slider.setMaximum(to);
		slider.setPaintTicks(true);
		slider.setMinorTickSpacing(to/20);
		slider.setMajorTickSpacing(to/10);
		setBoundOfComponent(slider, left_side, row,150,30);
		slider.setName(title);
		if(slider.getChangeListeners().length == 0)
			slider.addChangeListener(this);
		add(slider);
	}
	
	private void createActionButton(boolean left_side, int current_row, JButton button, String title){
		button.setText(title);
		button.setActionCommand(title);
		if(button.getActionListeners().length == 0)
		button.addActionListener(this);
		setBoundOfComponent(button,left_side,current_row,175,30);
		add(button);
	}
	
	private void createCheckbox(boolean left_side,int current_row, JCheckBox jcb, String title) {
		jcb.setText(title);
		setBoundOfComponent(jcb, left_side, current_row,175,30);
		jcb.setActionCommand(title);
		if(jcb.getActionListeners().length == 0)
		jcb.addActionListener(this);		
		add(jcb);
	}

	private void createRadioButtons(boolean left_side,int current_row, JRadioButton[] buttons, String[] titles){
		ButtonGroup group = new ButtonGroup();
		for(int i = 0; i < buttons.length; i++){
			buttons[i].setText(titles[i]);
			setBoundOfComponent(buttons[i],left_side,current_row++,200,30);
			buttons[i].setActionCommand(titles[i]);
			if(buttons[i].getActionListeners().length == 0)
			buttons[i].addActionListener(this);
			group.add(buttons[i]);
			add(buttons[i]);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e){
		
		switch(e.getActionCommand()){
		case "Importiere Graph":
			if(BwInf.Control.thread_running){
			JOptionPane.showMessageDialog(null, "Da gerade ein Algorithmus läuft, können zurzeit keine Funktionen ausgeführt werden.");
			return;
			}
			BwInf.Import_graph.import_graph();
			break;
		case "4) Alle Knoten erreichbar":
			BwInf.Control.setTask(BwInf.Control.TASK_ALL_REACHABLE);
			break;
		case "5) Größter Unterschied (HEU)":
			BwInf.Control.setTask(BwInf.Control.TASK_BIGGEST_CHANGE_HEU);
			break;
		case "5) Größter Unterschied (BF)":
			BwInf.Control.setTask(BwInf.Control.TASK_BIGGEST_CHANGE_BF);
			break;
		case "3.1) Kürzester Weg":
			BwInf.Control.setTask(BwInf.Control.TASK_PATH);
			break;
		case "3.2) Kürzester Weg (Illegal)":
			BwInf.Control.setTask(BwInf.Control.TASK_PATH_ILLEGALTURNS);
		 break;
		case "Beliebig Abbiegen":
			BwInf.Control.consider_all_directions_graph = true;
			break;
		case "Rechts Abbiegen":
			BwInf.Control.consider_all_directions_graph = false;
			break;
		case "Aufgeteilter Graph":
			JCheckBox split_graph = (JCheckBox) e.getSource();
			if(split_graph.isSelected())
				GUI_Graph.displayDGraph(BwInf.Control.original_graph_right,false);
			else
				GUI_Graph.displayDGraph(BwInf.Control.original_graph, false);
			break;
		case "Gehe zu Knoten":
			GUI_Graph.visit_vertex_user();
			GUI_Graph.paint();
			break;
		case "Zentrieren und Skalieren":
			GUI_Graph.centralize_camera();
			GUI_Graph.paint();
			break;
		case "Abbrechen":
			if(Control.algorithm_thread != null && Control.thread_running){
				Control.algorithm_thread.stop();
				algorithm_progress.setValue(0);
				Control.thread_running = false;
				solution_text_field.setText("Abgebrochen!");
			}
			break;
		case "Entfernung":
			GUI_Graph.capacity_is_distance = ((JRadioButton) e.getSource()).isSelected();
			GUI_Graph.paint();
			break;
		case "Knotenanzahl":
			GUI_Graph.capacity_is_distance = !((JRadioButton) e.getSource()).isSelected();
			GUI_Graph.paint();
			break;
		case "Kantenkapazitäten zeigen":
			JCheckBox box = (JCheckBox) e.getSource();
			GUI_Graph.setShowCapacites(box.isSelected());
			GUI_Graph.paint();
			break;
		case "Kantenwinkel zeigen":
			JCheckBox box2 = (JCheckBox) e.getSource();
			GUI_Graph.setShowVertexAngles(box2.isSelected());
			GUI_Graph.paint();
			break;
		case "Knotennamen zeigen":
			JCheckBox box3 = (JCheckBox) e.getSource();
			GUI_Graph.setShowVertexName(box3.isSelected());
			GUI_Graph.paint();
			break;
		case "Löse Aufgabe":
			if(BwInf.Control.thread_running){
				JOptionPane.showMessageDialog(null, "Da gerade ein Algorithmus läuft, können zurzeit keine Funktionen ausgeführt werden.");
				return;
			}
			BwInf.Control.solve();
			break;
		case "Zufälliger Graph":
			BwInf.MapGenerator.generateRandomMap();
			break;
		}
	}
	
	public void stateChanged(ChangeEvent e){
		switch(((Component) e.getSource()).getName()){
		case "Knotendichte":
			GUI_Graph.setVertexDistance(((JSlider) e.getSource()).getValue());
			GUI_Graph.positionVertices(false);
			GUI_Graph.paint();
			break;
		case "Slide-Tempo":
			GUI_Graph.setScrollSpeed(((JSlider) e.getSource()).getValue());
			break;
		case "Zoom-Tempo":
			GUI_Graph.setZoomSpeed(((JSlider) e.getSource()).getValue());
			break;
		}
	}
	
	private static int row_y(int current_row) {
		return (current_row-1)*ROW_DISTANCE;
	}

	private void setBoundOfComponent(Component comp, boolean left_side, int row, int width, int height){
		int ypos = row_y(row);
		if(left_side)
			comp.setBounds(OFFSET_RIGHTAREA_X,OFFSET_RIGHTAREA_Y+ypos,width,height);
		else
			comp.setBounds(0,OFFSET_RIGHTAREA_Y+ypos, width, height);
	}
	
	private void setBoundOfComponent(Component comp, boolean left_side, int row, int width, int height, int einrueck){
		int ypos = row_y(row);
		if(left_side)
			comp.setBounds(OFFSET_RIGHTAREA_X+einrueck,OFFSET_RIGHTAREA_Y+ypos,width,height);
		else
			comp.setBounds(einrueck,OFFSET_RIGHTAREA_Y+ypos, width, height);
	}
	
	private void createDrawArea() {
		GUI_Graph.GRAPHICS.setBounds(OFFSET_DRAWAREA_X,OFFSET_DRAWAREA_Y,DRAWAREA_WIDTH,DRAWAREA_HEIGHT);
		add(GUI_Graph.GRAPHICS);
	}


}