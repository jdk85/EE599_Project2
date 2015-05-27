package edu.nau.ee599;


import java.io.File;
import java.io.FileWriter;
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
//		(new Project2()).run();
//		(new Project2()).fake();
//		(new Project2()).plotFake();
//		(new Project2()).plotArb();
		(new Project2()).predictAll();
	}
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

		try{				
			System.out.println("\r\n\r\n");
			//Filter data by start and end date
			//as well as start and end hour
			ArrayList<Node> shortList = DataProcessor.getNodeSetDataRange(
					sdf.parse(startTime).getTime(), 
					sdf.parse(endTime).getTime(),
					nodes,
					30); //Limit to only channels with more than 30 data points for the CLT
			
			//Print the number of data points for the filtered nodes
			
			for(Node s : shortList){	
				System.out.println(s.getName() + "\tData Points: " + s.getDataSize());	
			}
			

			//Temp store the arboretum node
			Node arboretum;
			//Check that the node list has an arboretum node
			if((arboretum = findNodeByName("arboretum")) == null){
				System.out.println("Arboretum Node Not Found... Exiting");
				System.exit(0);
			}
			double t0,t1,e0,delta,estTemp;
			long ts,ts1;
			FileWriter temperatures = new FileWriter("./EstimatedTemperatures.csv");
			temperatures.append("name,timestamp,estimated,actual,outdoor\n");
			//Iterate through each node
			for(Node n : shortList){
				
				//Make sure we're not using the arboretum node
				if(!n.getName().equals("arboretum")){
					//System.out.println("Node: " + n.getName());
					ArrayList<Tuple> data = n.getData();

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
						//Fetch the temperature for the current room temp (Th(delta+t))
						t1 = data.get(i).getTemperature();
						if(n.getMleTau() != 0){
							if((t0 > -50 && t0 < 50)
								&& (t1 > -50 && t1 < 50)
								&& (e0 > -50 && e0 < 50) 
								&& (delta > 0 && delta < 65)
							){
								//Populate the estimated indoor temp from MLE of calculated taus
								estTemp = t0 + (-delta*(t0-e0))/n.getMleTau();
								if(estTemp > -60 && estTemp < 60){
									temperatures.append(n.getName()+","+ts+","+ estTemp +","+t1+","+e0+"\n");
								}
								else{
									System.out.println("\t*" + n.getName()+"\tTS: "+ts+"\tDelta: " + delta +"\tEstimate: "+ estTemp +"\tt1: "+t1+"\te0: "+e0);
								}
								
							}
						}
					}
				}
			}
			temperatures.flush();
			temperatures.close();
			
		
		}
		catch(ParseException e){
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		
	}
	/**
	 * 
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
	public void plotFake(){
		double R = 5.88;
		double C = 3401;
		//Generate the dataset
		KnownRCData rcdata = new KnownRCData("Generated Indoor Temperature Data", R,C);
		rcdata.plotFakeData();
		
		
	}
	
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
		
		//Print the number of data points for each wisard to see if everything worked
		/*
		for(Node n : nodes){
			System.out.println(n.getName() + "\tData Points: " + n.getDataSize());
		}
		*/
		
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
			
			//Print the number of data points for the filtered nodes
			
//			for(Node s : shortList){	
//				System.out.println(s.getName() + "\tData Points: " + s.getDataSize());	
//			}
			
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
			//TODO: take the tau arrays from each node and find ML
			
		}
		catch(ParseException e){
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void estimateRC(ArrayList<Node> nodes){
		//Temp store the arboretum node
		Node arboretum;
		//Check that the node list has an arboretum node
		if((arboretum = findNodeByName("arboretum")) == null){
			System.out.println("Arboretum Node Not Found... Exiting");
			System.exit(0);
		}
		double tau,t0,t1,e0,delta,estTemp;
		long ts,ts1;
		try{
			FileWriter temperature = new FileWriter("TemperatureData.csv");
			FileWriter estimatedTemperature = new FileWriter("EstimatedIndoor.csv");
			temperature.append("name,timestamp,indoor,outdoor\n");
			estimatedTemperature.append("name,timestamp,indoor\n");
			//Iterate through each node
			for(Node n : nodes){
				
				//Make sure we're not using the arboretum node
				if(!n.getName().equals("arboretum")){
					//System.out.println("Node: " + n.getName());
					ArrayList<Tuple> data = n.getData();
					
					
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
										//System.out.println("tau: " + tau + "\tt0:" + t0 + "\tt1: " + t1 + "\te0: " + e0 + "\tdelta: " + delta + " " + new Date(ts));							
										n.addTausTuple(new Tuple(ts,tau));
										//Populate the estimated indoor temp from MLE of calculated taus
										estTemp = t0 + (-delta*(t0-e0))/n.getMleTau();
										
										estimatedTemperature.append(n.getName()+","+ts+","+ estTemp + "\n");
										
										temperature.append(n.getName()+","+ts+","+t0+","+e0+"\n");
									}
								}
							}
						}
					}
				}
			}
			estimatedTemperature.flush();
			estimatedTemperature.close();
			temperature.flush();
			temperature.close();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public double getOutdoorTempAtTimeT(Node outdoor, long timestamp){
		//We need to search through the outdoor data array and get the closest timestamp
		//This is highly inefficient (O(N)) since the data is already sorted so it's a 
		//temporary solution
		
		//Init the closest Tuple object
		Tuple closestTuple = null;
		long difference = Long.MAX_VALUE;
		long tempDifference;
		for(Tuple t : outdoor.getData()){
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
						tempNode.addDataTuple(new Tuple(Long.parseLong(csvrecord.get(2)),Double.parseDouble(csvrecord.get(6))));
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
					tempNode.addDataTuple(new Tuple(Long.parseLong(csvrecord.get(2)),Double.parseDouble(csvrecord.get(6))));
				}
			}
			System.out.println("Done Parsing Arboretum Data...");
		}catch(IOException e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * 
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
	 * 
	 * @param name
	 * @return
	 */
	public Node findNodeByName(String name){
		for(Node n : nodes){
			if(n.getName().equals(name))
				return n;
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public Node findNodeByName(ArrayList<Node> tempNodes, String name){
		for(Node n : tempNodes){
			if(n.getName().equals(name))
				return n;
		}
		
		return null;
	}
	
	
}// end class
