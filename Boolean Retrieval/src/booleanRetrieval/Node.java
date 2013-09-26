package booleanRetrieval;

import java.util.ArrayList;

/*
 * Implement data structure and respective interface for node storage
 * This class contains Name member for node name store, an arraylist to store all sonNode of this node and
 * a isOperator member to save whether this node is an operator(1) or not(0).
 */


public class Node {
	protected String Name = "";
	protected ArrayList<Node> sonNode = new ArrayList<Node>();
	protected int isOperator = -1;
	protected String Field = "";

	public Node() {

	}

	public Node(String Name) {
		this.Name = Name;
	}

	public String getName() {
		return Name;
	}
	public void setName(String Name) {
		this.Name = Name;
	}

	public ArrayList<Node> getSonNode() {
		return sonNode;
	}
	public void addSonNode(Node sonNodeName) {
		this.sonNode.add(sonNodeName);
	}

	public int getIsOperator() {
		return isOperator;
	}
	public void setIsOperator(int isOperator) {
		this.isOperator = isOperator;
	}
	public String getField() {
		return Field;
	}
	public void setField(String Field) {
		this.Field = Field;
	}


}