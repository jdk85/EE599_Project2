package edu.nau.ee599;

import java.util.ArrayList;


/**
 * Data processing class; contains methods for parsing data
 * 
 * @author mm863
 *
 */
public class DataProcessor {
	
	/**
	 * Default contstructor
	 */
	public DataProcessor(){
		// anything?
	}

	
	/**
	 * returns a single node whose data is a list of tuples of data within the correct time interval
	 * 
	 * @param startTime
	 * @param endTime
	 * @param node
	 * @return
	 */
	public static Node getSingleNodeDataRange(long startTime, long endTime, Node node){
		// declare temp node which is a copy of the passed in node
		Node tempNode = node;
		// set data on temp node to be a subset of passed node's data from correct interval
		tempNode.setData(node.getDataRange(startTime, endTime));
		// return temp node
		return tempNode;
	}
	
	
	/**
	 * returns an arraylist of Nodes whose data is a list of tuples of data within the correct time interval
	 * 
	 * @param startTime
	 * @param endTime
	 * @param nodes
	 * @return
	 */
	public static ArrayList<Node> getNodeSetDataRange(long startTime, long endTime, ArrayList<Node> nodes){
		// arraylist to hold return values
		ArrayList<Node> returnList = new ArrayList<Node>();
		
		// loop through all passed-in nodes
		for(int i = 0; i < nodes.size(); i++){
			// temp node to copy current node's contents into
			Node tempNode = nodes.get(i);
			
			// get data range from range for this node
			tempNode.setData(tempNode.getDataRange(startTime, endTime));
			
			// add temp node to return list
			returnList.add(tempNode);
		}
		
		// return an arraylist of all nodes, each of which's data is an arraylist of tuples from the appropriate data range
		return returnList;
	}

	/**
	 * returns an arraylist of Nodes whose data is a list of tuples of data within the correct time interval
	 * 
	 * @param startTime
	 * @param endTime
	 * @param nodes
	 * @return
	 */
	public static ArrayList<Node> getNodeSetDataRange(long startTime, long endTime, int start_hour, int end_hour, ArrayList<Node> nodes, int threshold){
		// arraylist to hold return values
		ArrayList<Node> returnList = new ArrayList<Node>();
		
		// loop through all passed-in nodes
		for(int i = 0; i < nodes.size(); i++){
			// temp node to copy current node's contents into
			Node tempNode = nodes.get(i);
			
			// get data range from range for this node
			tempNode.setData(tempNode.getDataRange(startTime, endTime,start_hour,end_hour));
			
			if(tempNode.getDataSize() > threshold){
				// add temp node to return list
				returnList.add(tempNode);
			}
			
		}
		
		// return an arraylist of all nodes, each of which's data is an arraylist of tuples from the appropriate data range
		return returnList;
	}
	/**
	 * returns an arraylist of tuples with the min and max temps over a given interval for a single node
	 * First tuple in arraylist: min, Second tuple in arraylist: max
	 * 
	 * @param startTime
	 * @param endTime
	 * @param node
	 * @return
	 */
	 public static ArrayList<Tuple> getMinMaxRangeData(long startTime, long endTime, Node node){
		// arraylist to hold min and max values
		ArrayList<Tuple> returnList = new ArrayList<Tuple>();
		long time = 0;// dummy values
		long temp = 0;// dummy values
		Tuple min = new Tuple(time,temp); // tuple for minimum value on range
		Tuple max = new Tuple(time,temp); // tuple for maximum value on range
		Node tempNode = node; // temp node to work on
		
		// get subset range of data values
		tempNode.setData(node.getDataRange(startTime,endTime));
		
		Tuple minTemp = node.getData().get(0); // set to min for starting point
		Tuple maxTemp = node.getData().get(0); // set to max for starting point
		
		// loop through node's data to find min and max
		for(int i = 0; i < tempNode.getDataSize(); i++){
			// if a new min temp is found
			if(tempNode.getData().get(i).getTemperature() < minTemp.getTemperature()){
				// set current node to new min
				minTemp = tempNode.getData().get(i); 
			}
			// if a new max temp is found
			else if(tempNode.getData().get(i).getTemperature() > maxTemp.getTemperature()){
				// set current node to new max
				maxTemp = tempNode.getData().get(i); 
			}
			else 
				// no new min or max so continue to next point
				continue;
		}
		
		// add min then max tuples to arraylist
		returnList.add(min);
		returnList.add(max);
		return returnList;
	}

	 
	 /**
	  * returns an arraylist of an arraylist of tuples with the min and max temps over a given interval for a single node, for all nodes in the arraylist
	  * 
	  * @param startTime
	  * @param endTime
	  * @param nodeList
	  * @return
	  */
	public static ArrayList<ArrayList<Tuple>> getMinMaxRangeDataSet(long startTime, long endTime, ArrayList<Node> nodeList){
		ArrayList<ArrayList<Tuple>> returnArrayList = new ArrayList<ArrayList<Tuple>>();
		
		// loop through all tuple arraylists in the arraylist of nodes
		for(int i = 0; i < nodeList.size(); i++){
			// put min/max value tuples at the first index of the arraylist of arraylists
			returnArrayList.add(getMinMaxRangeData(startTime, endTime, nodeList.get(i)));
		}
		
		// return the arraylist
		return returnArrayList;
	}
}
