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
import org.jfree.ui.RefineryUtilities;

public class Project2 {

	/** Holds all the node objects (one per wisard) */
	private ArrayList<Node> nodes;
	
	
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
		//This just loads and plots the fake data
		//plotFakeData(0.5,500);
		
		//Create the node Array
		nodes = new ArrayList<Node>();
		for(int i = 1; i <= 19; i++){
			nodes.add(new Node("wisard_" + i,i));
		}
		try{
			File adelData = new File("./AdelMathDeployment.csv");
			//File arbData = new File("./ArbTempdataset.csv");		
			
			CSVParser parser = CSVParser.parse(adelData, Charset.defaultCharset(), CSVFormat.RFC4180);
			
			System.out.println("Parsing data...");
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
			System.out.println("Done parsing data...");
			
			
//			//Print out all the values for wisard 3 just to see if it worked
//			if((tempNode = findNodeByName("wisard_3")) != null){
//				for(Tuple t : tempNode.getData()){
//					System.out.println(sdf.format(new Date(t.getTimestamp())) + " "  + t.getTimestamp());
//				}
//			}
			
			
			//Print the number of data points for each wisard to see if everything worked
			for(Node n : nodes){
				System.out.println(n.getName() + "\tData Points: " + n.getDataSize());
			}
			
			
			// test data processor methods
			DataProcessor proc1 = new DataProcessor();
			ArrayList<Node> shortList = nodes;
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
			
			//Define the start time
			//String start_date = "04/03/2015 12:00:00";
			//Parse the start time into ms since 1970
			//long start_time = sdf.parse(start_date).getTime();
			
			String startTime = "04/10/2015 12:00:00";
			String endTime = "04/10/2015 12:00:00";
			try{
				shortList = proc1.getNodeSetDataRange(sdf.parse(startTime).getTime(), sdf.parse(endTime).getTime(), shortList);
				for(Node s : shortList){
					System.out.println(s.getName() + "\tData Points: " + s.getDataSize());
				}
			}
			catch(ParseException e){
				e.printStackTrace();
				System.exit(0);
			}
			
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
	/**
	 * Generates a plot of outdoor, indoor, thermostat temperature values
	 * using the RC that is passed to this function 
	 */
	public void plotFakeData(double R, double C){
		KnownRCData rcdata = new KnownRCData("Generated Outdoor Temperature Data", R,C);
		rcdata.pack();
		RefineryUtilities.centerFrameOnScreen(rcdata);
		rcdata.setVisible(true);	
	}
	
	
}// end class
