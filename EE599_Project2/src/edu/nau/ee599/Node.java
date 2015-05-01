package edu.nau.ee599;
//************************************************************************************
// File: Node.java
// Description: This file contains the class definitions for the data items for
// 				project 2 including the related methods
//************************************************************************************

// includes
import java.util.ArrayList;

// Node class definition
public class Node {
	// class variables
	private String name;
	private int id;
	private ArrayList<Tuple> data;
	
	// constructor
	public Node(String name, int id){
		this.setName(name); 		// name: ex. "Wisard_10"
		this.setId(id);			// id: ex. 10
		
		//Init the Arraylist
		data = new ArrayList<Tuple>();
	}// end constructor
	
	
	public ArrayList<Tuple> getData(){
		return data;
	}
	// retrieves the number of data elements in the Node's data
	public int getDataSize(){
		return data.size();
	}// end getNumDataPoints

	/**
	 * Add a tuple to the data object for this node
	 * @param tuple
	 */
	public void addTuple(Tuple tuple){
		data.add(tuple);	
	}



	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}



	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}



	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
}// end class