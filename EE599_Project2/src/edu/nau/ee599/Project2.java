package edu.nau.ee599;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Project2 {

	/** Holds all the node objects (one per wisard) */
	private ArrayList<Node> nodes;
	
	private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	
	/**
	 * Main method - creates new instance of project 2 and executes run()
	 * @param args
	 */
	public static void main(String[] args){
		(new Project2()).run();
	}
	
	/**
	 * Main execution method
	 */
	public void run(){
		/*double R = 0.5;
		double C = 500;
		//Generate the dataset
		KnownRCData rcdata = new KnownRCData("Generated Outdoor Temperature Data", R,C);
		//rcdata.plotFakeData();
		rcdata.estimateRC();
		*/
		
		//Create the node Array
		nodes = new ArrayList<Node>();
		//Add the arb data as the first entry with -1 id
		nodes.add(new Node("arboretum",-1));
		//Populate node list for wisards
		for(int i = 1; i <= 19; i++){
			nodes.add(new Node("wisard_" + i,i));
		}
		
		parseAdel();
		parseArb();
		//Print the number of data points for each wisard to see if everything worked
		for(Node n : nodes){
			System.out.println(n.getName() + "\tData Points: " + n.getDataSize());
		}
		
		/////////////////////////////////////
		//
		//		Process Data
		//
		////////////////////////////////////
		
		String startTime = "04/10/2015 21:00:00";
		String endTime = "04/17/2015 5:00:00";
		int start_hour = 21;
		int end_hour = 5;
		try{				
			System.out.println("\r\n\r\n");
			//Filter data by start and end date
			//as well as start and end hour
			ArrayList<Node> shortList = DataProcessor.getNodeSetDataRange(
					sdf.parse(startTime).getTime(), 
					sdf.parse(endTime).getTime(), 
					start_hour, 
					end_hour, 
					nodes,
					500); //Limit to only channels with more than 500 data points
			for(Node s : shortList){	
				System.out.println(s.getName() + "\tData Points: " + s.getDataSize());	
			}
			
			estimateRC(shortList);
			//TODO: take the tau arrays from each node and find ML
			
		}
		catch(ParseException e){
			e.printStackTrace();
			System.exit(0);
		}
			
		
		
		
		
	}
	public void estimateRC(ArrayList<Node> nodes){
		Node arboretum = findNodeByName("arboretum");
		if(arboretum == null){
			System.out.println("Arboretum Node Not Found... Exiting");
			System.exit(0);
		}
		else{
			nodes.remove(arboretum);
		}
		double tau,t0,t1,e0;
		for(Node n : nodes){
			System.out.println("Node: " + n.getName());
			ArrayList<Tuple> data = n.getData();
			
			//Use this equation:
			//tau = (-delta*(t0-e0))/(t1-t0)
			//
			//Where
			//delta: time step (seconds)
			//t0 = indoor temp at time t
			//t1 = indoor temp at time t+delta
			//e0 = outdoor temp at time t
			
			for(int i = 0; i < data.size()-1; i++){
				
				System.out.println("t0:" + data.get(i).getTemperature() + "\tt1:" + data.get(i+1).getTemperature());
				//TODO: get e0 for the closest timestamp to t0's timestamp
				//TODO: calculate tau using above equation
				//TODO: add a tau array to each node (add it to the node class)
				//TODO: populate each tau array for each node 
				
			}
		}
	}
	/**
	 * 
	 */
	public void parseAdel(){
		try{
			File adelData = new File("./AdelMathDeployment.csv");
			//File arbData = new File("./ArbTempdataset.csv");		
			
			CSVParser parser = CSVParser.parse(adelData, Charset.defaultCharset(), CSVFormat.RFC4180);
			
			System.out.println("Parsing Adel Data...");
			//Placeholders to populate node array
			String tempName,name;
			int tempIndex;
			Node tempNode;
			for(CSVRecord csvrecord : parser){				
				//Get the wisard id and module id as strings
				tempIndex = csvrecord.get(0).indexOf("wisard_");
				//Check that we have a match
				if(tempIndex != -1){
					//Get the index of the next forward slash
					tempName = csvrecord.get(0).substring(tempIndex);		
					//Slice out the wisard id
					name = tempName.substring(0,tempName.indexOf("/"));
					
					//Find the corresponding node and add the timestamp/data tuple
					if((tempNode = findNodeByName(name)) != null){
						tempNode.addTuple(new Tuple(Long.parseLong(csvrecord.get(2)),Double.parseDouble(csvrecord.get(6))));
					}
				}
				
			}
			System.out.println("Done Parsing Adel data...");
		}catch(IOException e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * 
	 */
	public void parseArb(){
		try{
			File arbData = new File("./ArbTempdataset.csv");		
			
			CSVParser parser = CSVParser.parse(arbData, Charset.defaultCharset(), CSVFormat.RFC4180);
			
			System.out.println("Parsing Arboretum Data...");

			Node tempNode;
			for(CSVRecord csvrecord : parser){
				//Find the corresponding node and add the timestamp/data tuple
				if((tempNode = findNodeByName("arboretum")) != null){
					tempNode.addTuple(new Tuple(Long.parseLong(csvrecord.get(2)),Double.parseDouble(csvrecord.get(6))));
				}
			}
			System.out.println("Done Parsing Arboretum Data...");
		}catch(IOException e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	public Node findNodeByName(String name){
		for(Node n : nodes){
			if(n.getName().equals(name))
				return n;
		}
		
		return null;
	}
	
	
	
}// end class
