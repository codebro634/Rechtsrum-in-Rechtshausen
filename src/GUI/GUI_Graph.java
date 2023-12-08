
package GUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Solver.DGraph;
import Solver.DGraph_all_directions;
import Solver.Edge;
import Solver.Vertex;
import Solver.Vertex_right_turn;

public class GUI_Graph extends JPanel{

	/*
	 * Klasse zur grafischen Ausgabe eines DGraph
	 */
	
		private static final long serialVersionUID = 8585089991162884765L;

		static final GUI_Graph GRAPHICS = new GUI_Graph();
		
		static final Events events = new Events();
		
		private static BufferedImage BACKGROUND;
		
		GUI_Graph(){
			try {
				BACKGROUND = ImageIO.read(getClass().getClassLoader().getResourceAsStream("BACKGROUND.jpg"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private static int vertex_distance = 100;
		
		/*
		 * Setzen des Abstandes der einzelnen Knoten
		 */	
		static void setVertexDistance(int vertex_distance){
			if(vertex_distance < 1) return;
				GUI_Graph.vertex_distance = vertex_distance;
		}
		
		/*
		 * Knotengröße und Knotenrandgröße
		 */
		private static final int vertex_size = 50;
		
		private static final int vertex_frame_size = vertex_size/7;
		
		
		static int getVertexSize(){
			return vertex_size;
		}
		
		private static float scroll_speed = 1;
		
		/*
		 * Setzen der Geschwindigkeit
		 * mit der man sich im DGraphen
		 * bewegen kann
		 */	
		static void setScrollSpeed(float scroll_speed_in_percent){
			if(scroll_speed_in_percent < 1 || scroll_speed_in_percent > 100)
				return;
			GUI_Graph.scroll_speed = 0.02f*scroll_speed_in_percent;
		}
		
		private static float zoom_speed = 2f;
		
		/*
		 * Setzen der Geschwindigkeit
		 * mit der man herein- und heraus-
		 * zoomen kann.
		 */	
		static void setZoomSpeed(float zoom_speed_percent){
			if(zoom_speed_percent < 1 || zoom_speed_percent > 100)
				return;
			zoom_speed = 0.002f*zoom_speed_percent+1;
		}
		
		private static boolean show_vertex_name = true;
		
		/*
		 * Rückggabe, ob der Knotenname angezeigt wird
		 */
		static boolean isVertexNameDisplayed(){
			return show_vertex_name;
		}
		
		/*
		 * Wechsel zwischen Anzeigen des Knotennamens und nicht
		 */	
		static void setShowVertexName(boolean bool){
			show_vertex_name = bool;
		}
		
		private static boolean show_vertex_angles = false;
		
		/*
		 * Wechsel zwischen Anzeigen der Winkel der Kanten und nicht
		 */
		static void setShowVertexAngles(boolean bool){
			show_vertex_angles = bool;
		}
		
		private static boolean show_capacities = false;
		
		/*
		 * Setzten, ob Kantenkapazitäten angezeigt werden sollen oder nicht
		 */
		public static void setShowCapacites(boolean bool){
			show_capacities = bool;
		}
		
		public static boolean capacity_is_distance = true;

		
	private static DGraph displayed_DGraph = new DGraph_all_directions();
	
	static final int MAX_VERTEX_COUNT = 100001;
	
	private static boolean is_DGraph_too_big = false;
	
	/*
	 * Graph wird in das GUI geladen
	 */
	public static void displayDGraph(DGraph g, boolean centralize_camera){
		clearMarkedEdges();
		clearMarkedVertices();
		if(g == null) return;
		displayed_DGraph = g;
		if(g.getVertices().size() < MAX_VERTEX_COUNT)
		positionVertices(centralize_camera);
		is_DGraph_too_big = g.getVertices().size() >= MAX_VERTEX_COUNT;
		paint();
		if(is_DGraph_too_big)
			JOptionPane.showMessageDialog(null, "Die Anzahl der Knoten des Graphen übersteigen "+MAX_VERTEX_COUNT+". Daher wird der Graph nicht angezeigt.");
	}
	
	public static DGraph getDGraph(){
		return displayed_DGraph;
	}
	
	/*
	 * Positionen aller Knoten
	 */
	private static final HashMap<Vertex,VertexPositionData> vertices_positions = new HashMap<Vertex,VertexPositionData>();
	
	/*
	 * Färbung bestimmter Knoten
	 */
	private static HashMap<Vertex,Color> marked_vertices = new HashMap<Vertex,Color>();	
	
	public static HashMap<Vertex,Color> getMarkedVertices(){
		return marked_vertices;
	}
	
	public static void markVertex(String vertex, Color color){
		Vertex ver = displayed_DGraph.getVertex(vertex);
		if(ver != null)
		marked_vertices.put(ver,color);
	}

	public static void clearMarkedVertices(){
		marked_vertices.clear();
	}
	
	/*
	 * Färbung bestimmter Kanten
	 */
	private static HashMap<Edge,Color> marked_edges = new HashMap<Edge,Color>();
	
	public static void markEdge(String source,String sink, Color color){
		Vertex ver_source = displayed_DGraph.getVertex(source);
		Vertex ver_sink = displayed_DGraph.getVertex(sink);
		if(ver_source == null || ver_sink == null) return;
		marked_edges.put(ver_source.getOutgoingEdge(ver_sink), color);
		marked_edges.put(ver_sink.getOutgoingEdge(ver_source), color);
	}
	
	public static void clearMarkedEdges(){
		marked_edges.clear();
	}
	
	private static int matrix_size;
	
	static int getMatrixSize(){
		return matrix_size;
	}
	
	/*
	 * Positionieren aller Knoten
	 */
	static void positionVertices(boolean camera_setup){
		if(displayed_DGraph == null) return;
		vertices_positions.clear();
		ArrayList<Vertex> vertices = new ArrayList<Vertex>(displayed_DGraph.getVertices());
		for(Vertex ver : vertices){
			if(vertices_positions.containsKey(ver)) continue;
				if(!(ver instanceof Vertex_right_turn))
					vertices_positions.put(ver,new VertexPositionData((int)ver.getCoordinates().getX()*vertex_distance,-(int)ver.getCoordinates().getY()*vertex_distance,vertex_size));	
				else{
				Stack<Vertex> relatives = ((Vertex_right_turn) ver).getRelatives();
				double radius = vertex_size;
				double gaps = 2*Math.PI/(float)relatives.size();
				int middlepoint_x = (int) radius/2;
				int middlepoint_y = (int) radius/2;
				for(int i = 0;i < relatives.size(); i++){
					int x = (int) (middlepoint_x+Math.cos(i*gaps)*radius);
					int y = (int) (middlepoint_y+Math.sin(i*gaps)*radius);
					vertices_positions.put(relatives.get(i),new VertexPositionData((int) (ver.getCoordinates().x*vertex_distance+x),-(int) (ver.getCoordinates().y*vertex_distance+y),vertex_size));
				}
				}
		}
		if(camera_setup){
			centralize_camera();
		}
		
	}
	
	/*
	 * Die Kamera wird zentiert, so dass der gesamte Graph zu sehen ist.
	 */
	static void centralize_camera(){
		
		int min_x = Integer.MAX_VALUE,max_x = 0,min_y = Integer.MAX_VALUE,max_y = 0;
		for(Vertex ver : displayed_DGraph.getVertices()){
			int x = ver.getCoordinates().x;
			int y = ver.getCoordinates().y;
			if(x > max_x)
				max_x = x;
			if(x < min_x)
				min_x = x;
			if(y > max_y)
				max_y = y;
			if(y < min_y)
				min_y = y;
		}
		min_x*=vertex_distance;
		min_x-=vertex_frame_size;
		max_x*=vertex_distance;
		max_x+=vertex_frame_size;
		min_y*=vertex_distance;
		min_y-=vertex_frame_size;
		max_y*=vertex_distance;
		max_x+=vertex_frame_size;
		matrix_size =(int) (Math.pow((max_x-min_x)*(max_y-min_y),2));
		if(max_x-min_x > max_y-min_y)
		zoom_level = max_x-min_x+vertex_distance;
		else
		zoom_level = max_y-min_y+vertex_distance;
		x_scroll = 0;
		y_scroll = -max_y;
	}
	
	public static void paint(){
		GRAPHICS.repaint();
	}
	
	/*
	 * Male Kante
	 */
	private void drawEdge(Graphics g,Vertex target,float factor_X, float factor_Y, int source_x, int source_y, Color color, String text){
		g.setColor(color);
		g.setFont(new Font("Arial",Font.BOLD,(int) (vertex_frame_size*factor_X)));
		int[] target_coordinates = vertices_positions.get(target).pos;
		double target_x =(int) ((target_coordinates[0]-x_scroll)*factor_X+(vertex_size*factor_X/2));
		double target_y =(int) ((target_coordinates[1]-y_scroll)*factor_Y+(vertex_size*factor_Y/2));
		g.drawLine(source_x,source_y,(int)target_x,(int)target_y);
		if(show_capacities){
			g.setFont(new Font("Arial",Font.BOLD,(int) (vertex_frame_size*factor_X*2)));
			double capacity_x = (target_x-source_x)/2;
			double capacity_y = (target_y-source_y)/2;
			g.setColor(Color.BLACK);
			g.drawString(text, source_x+(int)capacity_x, source_y+(int)capacity_y);
		}
	}
	
	/*
	 * Malen des gesamten geladenen Graphen
	 */
	public void paintComponent(Graphics g){
		g.clearRect(0,0,getWidth(),getHeight());
		g.drawImage(BACKGROUND,0,0,getWidth(), getHeight(),null);
		if(displayed_DGraph == null || is_DGraph_too_big)
			return;
		
		HashSet<Vertex> vertices = displayed_DGraph.getVertices();
		float factor_X = getWidth()/zoom_level;
		float factor_Y = getHeight()/zoom_level;
		
		Graphics2D g2 = (Graphics2D) g;
		
			g2.setStroke(new BasicStroke(vertex_frame_size/2*factor_X));
			
					
			for(Vertex ver : vertices){
				VertexPositionData data = vertices_positions.get(ver);
				int[] coordinates = data.pos;
				int screen_x =(int) ((coordinates[0]-x_scroll)*factor_X+(data.breadth*factor_X/2));
				int screen_y =(int) ((coordinates[1]-y_scroll)*factor_Y+(data.breadth*factor_Y/2));
					g.setColor(Color.BLACK);
				
				HashSet<Edge> outgoing_edges = ver.getOutgoingEdges();
				for(Edge i : outgoing_edges){
					Color color = Color.BLACK;
					if(marked_edges.containsKey(i))
						color = marked_edges.get(i);
					if(capacity_is_distance)
						drawEdge(g,i.targetVertex(),factor_X,factor_Y,screen_x,screen_y,color,""+i.getCapacity());
					else
						drawEdge(g,i.targetVertex(),factor_X,factor_Y,screen_x,screen_y,color,"1");
				}

			}
		
		for(Vertex ver : vertices){
			
			VertexPositionData data = vertices_positions.get(ver);
			
			int[] coordinates = data.pos;
			if(!new Rectangle((int)x_scroll,(int)y_scroll,(int)zoom_level,(int)zoom_level).intersects(new Rectangle(coordinates[0],coordinates[1],vertex_size,vertex_size)))
				continue;

			int screen_x =(int) ((coordinates[0]-x_scroll)*factor_X);
			int screen_y =(int) ((coordinates[1]-y_scroll)*factor_Y);
			
			boolean vertex_marked = marked_vertices.containsKey(ver);
			
			if(vertex_marked)
				g.setColor(marked_vertices.get(ver));
			else
				g.setColor(Color.WHITE);
			
			g.fillOval(screen_x,screen_y,(int) (data.breadth*factor_X),(int) (data.breadth*factor_X));
			g.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(vertex_frame_size*factor_X/2));
			g.drawOval(screen_x, screen_y,(int) (data.breadth*factor_X),(int) (data.breadth*factor_X));
	
			g.setColor(Color.BLACK);
			g.setFont(new Font("Arial",Font.BOLD,(int) (vertex_frame_size*factor_X*3)));
			
			if(show_vertex_name)
				g.drawString(ver.getName(),screen_x+(int) ( vertex_size*factor_X/3),screen_y+(int) (vertex_size*factor_X*0.65));
			
			if(show_vertex_angles){	
			String angles = "";
			for(double i : ver.angles())
				angles+=","+i+"°";
			g.setFont(new Font("Arial",Font.BOLD,(int) (vertex_frame_size*factor_X)));
			g.drawString(angles,screen_x+(int) ( vertex_size*factor_X/5),screen_y+(int) (vertex_size*factor_X*1.3));
			}
			
			
		}
		
	}
	
	 private static float zoom_level = 1f;
	
	 static float getZoomLevel(){
		 return zoom_level;
	 }
	 
	 /*
	  * Hereinzoomen
	  */ 
	 static void zoom_in(){
		float old_zoom_level = zoom_level;
		zoom_level/=zoom_speed;
		if(zoom_level < vertex_size)
			zoom_level = vertex_size;
		x_scroll+= (old_zoom_level-zoom_level)/2;
		y_scroll+= (old_zoom_level-zoom_level)/2;
	}
	
	 /*
	  * Herauszoomen
	  */
	static void zoom_out(){
		float old_zoom_level = zoom_level;
		zoom_level*=zoom_speed;
		if(zoom_level > matrix_size){
			zoom_level = matrix_size;
		}
		x_scroll-= (zoom_level-old_zoom_level)/2;
		y_scroll-= (zoom_level-old_zoom_level)/2;
	}
	
	private static float y_scroll;
	
	private static float x_scroll;
	
	/*
	 * Verschiebungen des DGraphen
	 */	
	static void scroll_right(){
		float percent = (float)GRAPHICS.getWidth()/100;
		float factor = zoom_level/(float)GRAPHICS.getWidth();
		x_scroll += scroll_speed*percent*factor;
	}
	
	static void scroll_left(){
		float percent = (float)GRAPHICS.getWidth()/100;
		float factor = zoom_level/(float)GRAPHICS.getWidth();
		x_scroll -= scroll_speed*percent*factor;
	}
	
	static void scroll_down(){
		float percent = (float)GRAPHICS.getHeight()/100;
		float factor = zoom_level/(float)GRAPHICS.getHeight();
		y_scroll += scroll_speed*percent*factor;
	}
	
	static void scroll_up(){
		float percent = (float)GRAPHICS.getHeight()/100;
		float factor = zoom_level/(float)GRAPHICS.getHeight();
		y_scroll += -scroll_speed*percent*factor;
	}
	
	/*
	 * Kamera wird auf einen Knoten zentriert
	 */
	private static void visit_vertex(Vertex vertex){
		if(!displayed_DGraph.getVertices().contains(vertex))
			return;
		
		int[] vertex_position = vertices_positions.get(vertex).pos;
		x_scroll = vertex_position[0];
		y_scroll = vertex_position[1];
		zoom_level = vertex_size;
		float old_zoom_level = zoom_level;
		zoom_level*=10;
		if(zoom_level > matrix_size){
			zoom_level = matrix_size;
		}
		x_scroll -= (zoom_level-old_zoom_level)/2;
		y_scroll -= (zoom_level-old_zoom_level)/2;
		
	}
	
	/*
	 * Zentrierung der Kamera auf einen Knoten
	 */
	static void visit_vertex_user(){
		visit_vertex(displayed_DGraph.getVertex( JOptionPane.showInputDialog("Knotenname eingeben.")));
	}
	
}

class Events implements MouseMotionListener, MouseWheelListener{
	
	/*
	 * Klasse zum Interagieren mit dem DGraph-Viewer
	 */
	
	//MouseMotion Events

	private static int[] last_point = new int[2];
	private static byte ticks = 0;
		
	/*
	 * Bewegen im DGraphen
	 */
	public void mouseDragged(MouseEvent e) {
		ticks++;
		if(ticks < 3)
			return;
		
		int diff_x = Math.abs(last_point[0]-e.getX());
		int diff_y = Math.abs(last_point[1]-e.getY());
		ticks = 0;
			
		if(diff_x > 1){
			
		if(e.getX()-last_point[0] < 0)
			GUI_Graph.scroll_right();
		else
			GUI_Graph.scroll_left();
		
			}
			
			if(diff_y > 1){		
				if(e.getY()-last_point[1] > 0)
					GUI_Graph.scroll_up();
				else
					GUI_Graph.scroll_down();		
			}
		
		last_point[0] = e.getX();
		last_point[1] = e.getY();
		
		GUI_Graph.paint();
		
	}

	public void mouseMoved(MouseEvent e) {}

	//MouseWheel Event
	
	/*
	 * Zooms
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(e.getWheelRotation() == -1)
			GUI_Graph.zoom_in();
		else
			GUI_Graph.zoom_out();
		
		GUI_Graph.paint();
	}
	
}

/*
 * Hilfsklasse zum Speichern von
 * Knotendaten innerhalb des GUI
 */
class VertexPositionData{
	
	final int[] pos;
	
	final int breadth;
	
	VertexPositionData(int x, int y, int breadth){
		pos = new int[] {x,y};
		this.breadth = breadth;
	}
	
}


