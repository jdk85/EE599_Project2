package edu.nau.ee599;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
		if(args.length != 1){
			printHelp();
		}
		else{
			String arg = args[0];
			if(arg.equalsIgnoreCase("filter")){
				(new Project2()).run();
			}
			else if(arg.equalsIgnoreCase("spoof")){
				(new Project2()).plotFake();
			}
			else if(arg.equalsIgnoreCase("predict")){
				(new Project2()).predictAll();
			}
			else if(arg.equalsIgnoreCase("arb")){
				(new Project2()).plotArb();
			}
		}
	}
	/**
	 * Prints argument info to screen and quits
	 */
	public static void printHelp(){
		System.out.println("You must include mode as the first argument");
		System.out.println("See the included README for more info on each mode");
		System.out.println("==== Modes ====");
		System.out.println("filter - this mode parses the data and outputs a csv with tau estimates for 4/10/2015- 4/17/2015 filtered from 9PM to 5AM");
		System.out.println("spoof - this mode generates and plots a spoof model");
		System.out.println("predict - this mode uses known MLE tau values for each wisard to predict the indoor temp");
		System.out.println("arb - this mode plots the arb data");
		System.out.println("\r\n\r\n");
		System.exit(-1);
	}
	/**
	 * This function looks at MLE tau estimates and tries to predict
	 * the next indoor temperature for the next t+delta
	 */
	public void predictAll(){
		//Create the node Array
		nodes = new ArrayList<Node>();
		//Add the arb data as the first entry with -1 id
		nodes.add(new Node("arboretum",-1));
		//Populate node list for wisards
		for(int i = 1; i <= 19; i++){
			nodes.add(new Node("wisard_" + i,i));
		}
		
		//Load Adel data into the node array
		parseAdel();
		//Load Arb data into the node array
		parseArb();
		//Load MLE tau estimates
		parseTau();
		
		String startTime = "04/10/2015 21:00:00";
		String endTime = "04/17/2015 5:00:00";
		int start_hour = 21;
		int end_hour = 5;
		try{
			System.out.println("\r\n\r\n");
			//Filter data by start and end date
			//as well as start and end hour
			ArrayList<Node> filterList = DataProcessor.getNodeSetDataRange(
					sdf.parse(startTime).getTime(), 
					sdf.parse(endTime).getTime(), 
					start_hour, 
					end_hour, 
					nodes,
					30); //Limit to only channels with more than 30 data points for the CLT

			

			//Temp store the arboretum node
			Node arboretum;
			//Check that the node list has an arboretum node
			if((arboretum = findNodeByName("arboretum")) == null){
				System.out.println("Arboretum Node Not Found... Exiting");
				System.exit(0);
			}
			//Placeholder variables
			double t0,t1,e0,delta,estTemp,prior;
			long ts,ts1;
			
			//CSV Filewriter
			FileWriter temperatures = new FileWriter("./EstimatedTemperatures.csv");
			temperatures.append("name,timestamp,estimated,actual,outdoor,timestring\n");
			//Iterate through each node
			for(Node n : filterList){
				
				//Make sure we're not using the arboretum node
				if(!n.getName().equals("arboretum")){
					//System.out.println("Node: " + n.getName());
					ArrayList<TripleTuple> data = n.getEstimates();
					
					data.get(0).setEstimatedTemperature(data.get(0).getTemperature());
					
					for(int i = 1; i < data.size(); i++){
						//Get the previous timestamp
						ts = data.get(i-1).getTimestamp();
						//Get the current timestamp
						ts1 = data.get(i).getTimestamp();
						delta = (ts1-ts)/1000.;
						//Get the arb data temperature for the closest timestamp to the current (Te(t))
						e0 = getOutdoorTempAtTimeT(arboretum, ts);
						//Fetch the temperature for previous room temp (Th(t))
						t0 = data.get(i-1).getTemperature();
						//Fetch the previous estimate for a room
						prior = data.get(i-1).getEstimatedTemperature();
						data.get(i).setEstimatedTemperature(prior);
						//Fetch the temperature for the current room temp (Th(delta+t))
						t1 = data.get(i).getTemperature();
						
						if(n.getMleTau() != 0){
							if((t0 > -50 && t0 < 50)
								&& (t1 > -50 && t1 < 50)
								&& (e0 > -50 && e0 < 50) 
								&& (delta > 0 && delta < 65)
							){
								//Populate the estimated indoor temp from MLE of calculated taus
								estTemp = prior + -(delta*(prior-e0))/n.getMleTau();
								System.out.println("Offset:\t"+(estTemp-t1));
								data.get(i).setEstimatedTemperature(estTemp);
								 
								if(estTemp > -60 && estTemp < 60){
									temperatures.append(n.getName()+","+ts+","+ estTemp +","+t1+","+e0+"," + new Date(ts) + "\n");
								}
								else{
									//System.out.println("\t*" + n.getName()+"\tTS: "+ts+"\tDelta: " + delta +"\tEstimate: "+ estTemp +"\tt1: "+t1+"\te0: "+e0);
								}
								
								
							}
							else{
								//If we have an invalid data point (or a big delta),
								//Set the current estimate to the actual indoor temperature
								data.get(i).setEstimatedTemperature(t0);
							}
						}
					}
				}
			}
			temperatures.flush();
			temperatures.close();

			System.out.println("Done with predictAll()");
		
		}
		catch(ParseException e){
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}
	/**
	 * Parses Arb data and plots it
	 */
	public void plotArb(){
		//Create the node Array
		nodes = new ArrayList<Node>();
		//Add the arb data as the first entry with -1 id
		nodes.add(new Node("arboretum",-1));
		parseArb();
		KnownRCData rcdata = new KnownRCData("Aboretum Outdoor Temperature Data");
		rcdata.plotNodeData(findNodeByName("arboretum"));		
		
	}
	/**
	 * Generates spoof data and plots it
	 */
	public void plotFake(){
		double R = 5.88;
		double C = 3401;
		//Generate the dataset
		KnownRCData rcdata = new KnownRCData("Generated Indoor Temperature Data", R,C);
		rcdata.plotFakeData();
		
		
	}
	/**
	 * Generates fake data and tries to estimate RC
	 */
	public void fake(){
		double R = 5.88;
		double C = 3401;
		//Generate the dataset
		KnownRCData rcdata = new KnownRCData("Generated Indoor Temperature Data", R,C);
		//rcdata.plotFakeData();
		rcdata.estimateRC();
		
	}
	/**
	 * Main execution method
	 * Filters the data and generates 'TauData' CSV
	 */
	public void run(){
				
		//Create the node Array
		nodes = new ArrayList<Node>();
		//Add the arb data as the first entry with -1 id
		nodes.add(new Node("arboretum",-1));
		//Populate node list for wisards
		for(int i = 1; i <= 19; i++){
			nodes.add(new Node("wisard_" + i,i));
		}
		
		//Load Adel data into the node array
		parseAdel();
		//Load Arb data into the node array
		parseArb();
		//Load MLE tau estimates
		parseTau();

		/////////////////////////////////////
		//
		//		Filter Data by Time
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
					30); //Limit to only channels with more than 30 data points for the CLT

			
			/////////////////////////////////////
			//
			//		Process Tau
			//
			////////////////////////////////////
			estimateRC(shortList);
			
			
			/////////////////////////////////////
			//
			//		Generate CSV
			//
			////////////////////////////////////
			FileWriter writer = new FileWriter("TauData.csv");
			writer.append("name,timestamp,tau\n");
			for(Node n : shortList){				
				for(Tuple t : n.getTaus()){
					writer.append(n.getName() + "," + t.getTimestamp() + "," + t.getTemperature() + "\n");
				}
			}
			writer.flush();
			writer.close();
			System.out.println("Done");
			
		}
		catch(ParseException e){
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	/**
	 * Tries to use the spoof data to estimate RC 
	 * NOTE: this still assumes q=0 since we haven't split RC
	 * @param nodes
	 */
	public void estimateRC(ArrayList<Node> nodes){
		//Temp store the arboretum node
		Node arboretum;
		//Check that the node list has an arboretum node
		if((arboretum = findNodeByName("arboretum")) == null){
			System.out.println("Arboretum Node Not Found... Exiting");
			System.exit(0);
		}
		double tau,t0,t1,e0,delta;
		long ts,ts1;
		try{
			FileWriter temperature = new FileWriter("TemperatureData.csv");
			temperature.append("name,timestamp,indoor,outdoor\n");
			//Iterate through each node
			for(Node n : nodes){
				
				//Make sure we're not using the arboretum node
				if(!n.getName().equals("arboretum")){
					//System.out.println("Node: " + n.getName());
					ArrayList<TripleTuple> data = n.getEstimates();
					
					
					/////////////////////////////////////
					//
					//		Calculate RC (Tau)
					//
					// Use this equation:
					// tau = (-delta*(t0-e0))/(t1-t0)
					//
					// Where
					// delta: time step (ts1-ts)/1000.0 in seconds
					// t0 = indoor temp at time t
					// t1 = indoor temp at time t+delta
					// e0 = outdoor temp at time t
					// ts = time t
					// ts1 = time at t+delta
					//
					//
					////////////////////////////////////
					for(int i = 1; i < data.size(); i++){
						//Get the previous timestamp
						ts = data.get(i-1).getTimestamp();
						//Get the current timestamp
						ts1 = data.get(i).getTimestamp();
						
						//Get the arb data temperature for the closest timestamp to the current (Te(t))
						e0 = getOutdoorTempAtTimeT(arboretum, ts);
						//Fetch the temperature for previous room temp (Th(t))
						t0 = data.get(i-1).getTemperature();
						//Fetch the temperature for the current room temp (Th(delta+t))
						t1 = data.get(i).getTemperature();
						
						//Filter out any outlier temperature values
						if((t0 > 0 && t0 < 60) || (t1 > 0 && t1 < 60)){
							delta = (ts1 - ts)/1000.;
							//make sure delta is positive and not the same timestamp
							if(delta > 0 && !(delta > 65)){
								if(t1-t0 != 0){
									tau = (-delta*(t0-e0))/(t1-t0);
									//Check for stability
									if(delta < tau){						
										n.addTausTuple(new Tuple(ts,tau));	
										temperature.append(n.getName()+","+ts+","+t0+","+e0+"\n");
									}
								}
							}
						}
					}
				}
			}
			temperature.flush();
			temperature.close();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Matches the closest temporal temperature data point
	 * @param outdoor - the Node object containing the arboretum data
	 * @param timestamp - the timestamp to search for
	 * @return temperature value of the closest timestamp
	 */
	public double getOutdoorTempAtTimeT(Node outdoor, long timestamp){
		//We need to search through the outdoor data array and get the closest timestamp
		//This is highly inefficient (O(N)) since the data is already sorted so it's a 
		//temporary solution
		
		//Init the closest Tuple object
		TripleTuple closestTuple = null;
		long difference = Long.MAX_VALUE;
		long tempDifference;
		for(TripleTuple t : outdoor.getEstimates()){
			tempDifference = Math.abs(t.getTimestamp() - timestamp);
			if( tempDifference < difference){
				difference = tempDifference;
				closestTuple = t;
			}			
		}
		
		//Debug testing
		//System.out.println("Search Date: " + new Date(timestamp) + "\tFound Date: " + new Date(closestTuple.getTimestamp()));
		
		return closestTuple.getTemperature();
	}
	/**
	 * Parse and filter the Adel CSV
	 */
	public void parseAdel(){
		try{
			File adelData = new File("./AdelMathDeployment.csv");
			
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
						tempNode.addTripleTuple(new TripleTuple(Long.parseLong(csvrecord.get(2)),Double.parseDouble(csvrecord.get(6)),Double.NaN));
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
	 * Parse and filter the arb data
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
					tempNode.addTripleTuple(new TripleTuple(Long.parseLong(csvrecord.get(2)),Double.parseDouble(csvrecord.get(6)),Double.NaN));
				}
			}
			System.out.println("Done Parsing Arboretum Data...");
		}catch(IOException e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * Parse and load in the MLE for tau
	 */
	public void parseTau(){
		try{
			File adelData = new File("./MLETau.csv");		
			
			CSVParser parser = CSVParser.parse(adelData, Charset.defaultCharset(), CSVFormat.RFC4180);
			
			System.out.println("Parsing MLE Tau Data...");
			//Placeholders to populate node array
			String name;
			Node tempNode;
			for(CSVRecord csvrecord : parser){		
				//Slice out the wisard id
				name = csvrecord.get(0);
				
				//Find the corresponding node and add the timestamp/data tuple
				if((tempNode = findNodeByName(name)) != null){
					tempNode.setMleTau(Double.parseDouble(csvrecord.get(1)));
				}
			}
				
			System.out.println("Done Parsing MLE Tau data...");			
			
		}catch(IOException e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * Searches the main node array for a particular node
	 * @param name
	 * @return the node if a match is found, else null
	 */
	public Node findNodeByName(String name){
		for(Node n : nodes){
			if(n.getName().equals(name))
				return n;
		}
		
		return null;
	}
	
	/**
	 * Searches the node array tempNodes for a particular node
	 * @param tempNodes
	 * @param name
	 * @return the node if a match is found, else null
	 */
	public Node findNodeByName(ArrayList<Node> tempNodes, String name){
		for(Node n : tempNodes){
			if(n.getName().equals(name))
				return n;
		}
		
		return null;
	}
	
	
}
