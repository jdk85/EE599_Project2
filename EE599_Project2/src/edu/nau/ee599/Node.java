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
	private ArrayList<Tuple> taus;
	private ArrayList<TripleTuple> estimates;
	private double mleTau;
	
	// constructor
	public Node(String name, int id){
		this.setName(name); 		// name: ex. "Wisard_10"
		this.setId(id);			// id: ex. 10
		
		//Init the Arraylist
		taus = new ArrayList<Tuple>();
		estimates = new ArrayList<TripleTuple>();
	}// end constructor
	

	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Tuple> getTaus(){
		return taus;
	}


	public void addTausTuple(Tuple tuple){
		taus.add(tuple);
	}
	public void addTripleTuple(TripleTuple tuple){
		estimates.add(tuple);	
	}

	/**
	 * 
	 * @return
	 */
	public int getEstimatesSize(){
		return estimates.size();
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
	public ArrayList<TripleTuple> getDataRange(long startTime, long endTime, int start_hour, int end_hour){
		// declare new ArrayList for result to be stored in. 
		ArrayList<TripleTuple> returnList = new ArrayList<TripleTuple>();
		//Calendar obj use to get hour from timestamp
		Calendar cal = Calendar.getInstance();
		//Stores the hour (0-23) of the current datapoint
		int hour;
		long timestamp;
		for(int i = 0; i < estimates.size(); i++){
			timestamp = estimates.get(i).getTimestamp();
			// if current data-point's time is after start but before end...
			if( timestamp >= startTime && timestamp <= endTime){
				//get the hour of the timestamp
				cal.setTime(new Date(timestamp));
				hour = cal.get(Calendar.HOUR_OF_DAY);				
				//Do we need to rollerover?
				if(start_hour > end_hour){
					if(hour >= start_hour || hour < end_hour){
						// get that data point and add to the return list
						returnList.add(estimates.get(i));
					}
				}
				else{
					if(hour >= start_hour && hour < end_hour){
						// get that data point and add to the return list
						returnList.add(estimates.get(i));
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
	public ArrayList<TripleTuple> getDataRange(long startTime, long endTime){
		// declare new ArrayList for result to be stored in. 
		ArrayList<TripleTuple> returnList = new ArrayList<TripleTuple>();
		
		long timestamp;
		
		for(int i = 0; i < estimates.size(); i++){
			timestamp = estimates.get(i).getTimestamp();
			// if current data-point's time is after start but before end...
			if(timestamp >= startTime && timestamp <= endTime){
				// get that data point and add to the return list
				returnList.add(estimates.get(i));
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

	/**
	 * @return the mleTau
	 */
	public double getMleTau() {
		return mleTau;
	}

	/**
	 * @param mleTau the mleTau to set
	 */
	public void setMleTau(double mleTau) {
		this.mleTau = mleTau;
	}

	/**
	 * @return the estPrior
	 */
	public ArrayList<TripleTuple> getEstimates() {
		return estimates;
	}

	/**
	 * @param estPrior the estPrior to set
	 */
	public void setEstimates(ArrayList<TripleTuple> estimates) {
		this.estimates = estimates;
	}
}// end class