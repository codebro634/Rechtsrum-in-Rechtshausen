package Solver;

import java.awt.Point;
import java.util.Stack;

public class Vertex_right_turn extends Vertex {

	/*
	 * Vertex_right_turn repräsentiert eine aufgeteilte Kreuzung.
	 */

	public Vertex_right_turn(Point coordinates, String name) {
		super(coordinates, name, true);
	}

	/*
	 * Falls dieser Knoten ein Überknoten ist, werden all seine Unterknoten hier
	 * gespeichert
	 */
	private Stack<Vertex> childreen = new Stack<Vertex>();

	/*
	 * Fügt dem Knoten einen weiteren Unterknoten hinzu.
	 */
	public void addChild(Vertex_right_turn child) {
		childreen.push(child);
		child.setParent(this);
	}

	/*
	 * Gibt alle Unterknoten dieses Knotens zurück.
	 */
	public Stack<Vertex> getChildreen() {
		return childreen;
	}

	/*
	 * Gibt den Überknoten dieses Knoten zurück, falls er selber keiner ist.
	 */
	private Vertex_right_turn parent;

	/*
	 * Gibt Überknoten zurück.
	 */
	public Vertex_right_turn getParent() {
		return parent;
	}

	/*
	 * Setzt den Überknoten.
	 */
	private void setParent(Vertex_right_turn Vertex_right_turn) {
		this.parent = Vertex_right_turn;
	}

	/*
	 * Gibt alle Knoten zurück, die den selben Überknoten besitzen. Ist dieser
	 * Knoten selber ein Überknoten, gibt er sich selber zurück.
	 */
	public Stack<Vertex> getRelatives() {
		if (parent == null) {
			Stack<Vertex> stack = new Stack<Vertex>();
			stack.add(this);
			return stack;
		}
		return parent.getChildreen();
	}

}
