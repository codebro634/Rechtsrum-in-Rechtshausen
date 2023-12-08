package Solver;

import java.awt.Point;
import java.util.Stack;

public class Vertex_right_turn extends Vertex {

	/*
	 * Vertex_right_turn repr�sentiert eine aufgeteilte Kreuzung.
	 */

	public Vertex_right_turn(Point coordinates, String name) {
		super(coordinates, name, true);
	}

	/*
	 * Falls dieser Knoten ein �berknoten ist, werden all seine Unterknoten hier
	 * gespeichert
	 */
	private Stack<Vertex> childreen = new Stack<Vertex>();

	/*
	 * F�gt dem Knoten einen weiteren Unterknoten hinzu.
	 */
	public void addChild(Vertex_right_turn child) {
		childreen.push(child);
		child.setParent(this);
	}

	/*
	 * Gibt alle Unterknoten dieses Knotens zur�ck.
	 */
	public Stack<Vertex> getChildreen() {
		return childreen;
	}

	/*
	 * Gibt den �berknoten dieses Knoten zur�ck, falls er selber keiner ist.
	 */
	private Vertex_right_turn parent;

	/*
	 * Gibt �berknoten zur�ck.
	 */
	public Vertex_right_turn getParent() {
		return parent;
	}

	/*
	 * Setzt den �berknoten.
	 */
	private void setParent(Vertex_right_turn Vertex_right_turn) {
		this.parent = Vertex_right_turn;
	}

	/*
	 * Gibt alle Knoten zur�ck, die den selben �berknoten besitzen. Ist dieser
	 * Knoten selber ein �berknoten, gibt er sich selber zur�ck.
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
