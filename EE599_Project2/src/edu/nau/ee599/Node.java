package edu.nau.ee599;
//************************************************************************************
// File: Node.java
// Description: This file contains the class definitions for the data items for
// 				project 2 including the related methods
//************************************************************************************

// includes
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

// Node class definition
public class Node {
	// class variables
	private String name;
	private int id;
	private ArrayList<Tuple> data;
	private ArrayList<Tuple> taus;
	
	// constructor
	public Node(String name, int id){
		this.setName(name); 		// name: ex. "Wisard_10"
		this.setId(id);			// id: ex. 10
		
		//Init the Arraylist
		data = new ArrayList<Tuple>();
		taus = new ArrayList<Tuple>();
	}// end constructor
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Tuple> getData(){
		return data;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Tuple> getTaus(){
		return taus;
	}
	
	/**
	 * retrieves the number of data elements in the Node's data
	 * @return
	 */
	public int getDataSize(){
		return data.size();
	}// end getNumDataPoints

	/**
	 * Add a tuple to the data object for this node
	 * @param tuple
	 */
	public void addDataTuple(Tuple tuple){
		data.add(tuple);	
	}
	public void addTausTuple(Tuple tuple){
		taus.add(tuple);
	}

	// sets a node's data to the specified arraylist
	public void setData(ArrayList<Tuple> dataSet){
		this.data = dataSet;
	}
	
	
	/**
	 * Return a range of data for a given start,end time filted to fall within the specified start
	 * and end hour
	 * 
	 * @param startTime
	 * @param endTime
	 * @param start_hour
	 * @param end_hour
	 * @return
	 */
	public ArrayList<Tuple> getDataRange(long startTime, long endTime, int start_hour, int end_hour){
		// declare new ArrayList for result to be stored in. 
		ArrayList<Tuple> returnList = new ArrayList<Tuple>();
		//Calendar obj use to get hour from timestamp
		Calendar cal = Calendar.getInstance();
		//Stores the hour (0-23) of the current datapoint
		int hour;
		long timestamp;
		for(int i = 0; i < data.size(); i++){
			timestamp = data.get(i).getTimestamp();
			// if current data-point's time is after start but before end...
			if( timestamp >= startTime && timestamp <= endTime){
				//get the hour of the timestamp
				cal.setTime(new Date(timestamp));
				hour = cal.get(Calendar.HOUR_OF_DAY);				
				//Do we need to rollerover?
				if(start_hour > end_hour){
					if(hour >= start_hour || hour < end_hour){
						// get that data point and add to the return list
						returnList.add(data.get(i));
					}
				}
				else{
					if(hour >= start_hour && hour < end_hour){
						// get that data point and add to the return list
						returnList.add(data.get(i));
					}
				}
			}
		}
		
		// return an array list of data in that range
		return returnList;
	}
	/**
	 * Return a range of data for a given start,end time
	 * @param startTime
	 * @param endTime
	 * @return ArrayList of data tuples
	 */
	public ArrayList<Tuple> getDataRange(long startTime, long endTime){
		// declare new ArrayList for result to be stored in. 
		ArrayList<Tuple> returnList = new ArrayList<Tuple>();
		
		// loop through node's data
		int dataIndex = 0;
		ArrayList<Tuple> NodeData = new ArrayList<Tuple>();
		NodeData = this.getData();
		
		while(NodeData.get(dataIndex) != null){
			// if current data-point's time is after start but before end...
			if(NodeData.get(dataIndex).getTimestamp()>= startTime && NodeData.get(dataIndex).getTimestamp() <= endTime){
				// get that data point and add to the return list
				returnList.add(NodeData.get(dataIndex));
			}
		}
		
		// return an array list of data in that range
		return returnList;
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