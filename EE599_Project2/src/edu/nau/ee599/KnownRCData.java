package edu.nau.ee599;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * This class generates a spoof data set that represents a building
 * temperature reading with a known time-constant tau
 * @author jdk85
 *
 */
public class KnownRCData extends ApplicationFrame{

	/** Implements serializable so this is required */
	private static final long serialVersionUID = -8407454106086882552L;
	
	/** Prints or parses a date object as "MM/dd/yyyy hh:mm:ss" */
	private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	/** The average minimum temperature in Flagstaff in April */ 
	private int minTemp = -3;
	/** The average max temperature in Flagstaff in April */
	private int maxTemp = 14;
	/** The title for the chart object */
	private String title;
	/** The tau parameters - resistance and capacitance of a building */
	private double R,C;
	
	/** Define the time step (in seconds) */
	double delta = 60;
	/** XYSeries to store the outdoor temperature data */
    private XYSeries outdoorTempData = new XYSeries("Outdoor Temperature"); 
	/** XYSeries object for the indoor temperature */
	private XYSeries indoorTempData = new XYSeries("Indoor Temperature"); 
	/** Create XYSeries object for the heater (or the heat source) */
	private XYSeries inflowTempData = new XYSeries("In-flow Temperature"); 
	
	/** XYDataset object - created by the constructor */
	private XYDataset dataset;
	
	/**
	 * Constructor - extends ApplicationFrame so we must call the super constructor
	 * Set the param variables here and create the dataset and the chart
	 * Finally, display the chart in a chart panel object
	 * @param title
	 * @param R
	 * @param C
	 */
	public KnownRCData(String title, double R, double C) {
		super(title);
		this.title=title;
		this.R = R;
		this.C = C;
		dataset = createDataset();		
	}

	public XYDataset getDataset(){
		return dataset;
	}
	
	/**
	 * Actually create the fake data set here..
	 * 
	 * The series are:
	 * Outdoor Temperature (sine wave)
	 * Target Thermostat Setting (line)
	 * In-flow Temperature (on/off depending on indoor temperature)
	 * Indoor Temperature (variable based on outdoor,thermostat, RC values)
	 * 
	 * @return
	 * @throws ParseException 
	 */
	private XYDataset createDataset() {
		try{
			//Number of samples - 2 weeks at a minute interval
			int samples = 1440*14;
			//Thermostat target temperature (degrees C)
			double thermo = 20.0;
			
			
			
			/////////////////////////////////////
			//
			//		Outdoor Temperature Data
			//				and
			//			Thermostat line
			//
			////////////////////////////////////
			
			//Create a new XYSeriesCollection to store the XYSeries objects
	        XYSeriesCollection data = new XYSeriesCollection();	        
			
	        //Create new XYSeries to store the thermostat line
	        XYSeries thermostat = new XYSeries("Thermostat");   
	        
			//Samples per period
	        double freq = 1440;
	        //Frequency
			double period = 1/freq;
			//Amplitude of the sine wave
			int amplitude = maxTemp - minTemp;
			//Placeholder - temporary temperature data
			double tempTemp;
			
			//Define the start time
			String start_date = "04/03/2015 12:00:00";
		
			//Parse the start time into ms since 1970
			long start_time = sdf.parse(start_date).getTime();
			
			System.out.println("Generating dataset starting at " + sdf.format(new Date(start_time)));
			
			//For each sample, calculate and populate the outdoor temperature data (model as sine wave)
			for(int i = 0; i < samples; i++){	
				//Calculate the temperature at time t
				tempTemp = (amplitude/2)*Math.sin((2*Math.PI)*i*period) + (amplitude/2) + minTemp;
				//Increment start time by a minute
				start_time += 60*1000;
				//We're just drawing a straight line
				thermostat.add(start_time,thermo);
				//Add xy point (time,temperature) to the outdoor temperature series
				outdoorTempData.add(start_time,tempTemp);
			}		
			
			
			System.out.println("\tOK");
			
			
			
			
			/////////////////////////////////////
			//
			//		Room temperature
			//
			////////////////////////////////////
			
			
			//Placeholders for temporary calculations
			double indoorVal,inputFlow;
			
			//Initialize the starting values for indoor temp and heater temp
			indoorTempData.add(outdoorTempData.getX(0), 18);
			inflowTempData.add(outdoorTempData.getX(0),0);
			
			//For each sample, determine if the heater is on/off and then calculate the room temperature
			for(int i = 1; i < samples; i++){
				//Use previous indoor temp value to determine whether or not to turn on the heater
				if(indoorTempData.getY(i-1).doubleValue() < thermo){
					inputFlow = 43.333;	//About 110 degrees F - typical heater output				
				}
				else{
					inputFlow = 0;
				}
				
				//Add the heater value to the in-flow series
				inflowTempData.add(outdoorTempData.getX(i), inputFlow);
				
				//Calculate the indoor room temperature based on outdoor, heater, and RC values
				indoorVal = indoorTempData.getY(i-1).doubleValue() + 
						(delta/C)*(inflowTempData.getY(i-1).doubleValue() - 
								(indoorTempData.getY(i-1).doubleValue() - outdoorTempData.getY(i-1).doubleValue())/R);
				//Add the room temperature to the indoor temperature series
				indoorTempData.add(outdoorTempData.getX(i),indoorVal);
			}
			
			//Add all the series to the collection
			data.addSeries(indoorTempData);
			data.addSeries(outdoorTempData);
			data.addSeries(thermostat);
			data.addSeries(inflowTempData);
			return data;
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		
		return null;
		
		
		
	}
	
	public void estimateRC(){
		double tau,prevIndoor,indoor,prevOutdoor,prevInflow;
		if(indoorTempData != null && outdoorTempData != null){
			for(int i = 1; i < indoorTempData.getItemCount(); i++){
				prevInflow = inflowTempData.getY(i-1).doubleValue();
				if(prevInflow == 0){
					prevIndoor = indoorTempData.getY(i-1).doubleValue();
					indoor = indoorTempData.getY(i).doubleValue();
					prevOutdoor = outdoorTempData.getY(i-1).doubleValue();					
					tau = (delta*-(prevIndoor - prevOutdoor))/(indoor - prevIndoor);
					System.out.println("Known: " + R*C + " Estimated: " + tau);
				}
			}
			
		}
		else{
			System.out.println("Dataset has not been generated!");
		}
	}
	/**
	 * This method creates a chart object 
	 * 	
	 * @param dataset
	 * @return chart
	 */
	public JFreeChart createChart( XYDataset dataset) {	        
        //Create the chart object
         JFreeChart chart = ChartFactory.createTimeSeriesChart(
        	title,      // chart title
            "Sample Time",                      // x axis label
            "Temperature in C",                      // y axis label
            dataset,                  // data
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );
        
        //Set bg color
        chart.setBackgroundPaint(Color.white);
        
        //Create new plot object from chart
        XYPlot plot = chart.getXYPlot();
        //Set color of plot
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        //Create renderer for displaying the plot
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        //For each series in the dataset...
        for(int i = 0; i < plot.getSeriesCount(); i++){
        	//Use lines instead of points unless it's the inflow temperature, then use points
        	if(!((String)plot.getDataset(0).getSeriesKey(i)).equals("In-flow Temperature")){       
        		renderer.setSeriesShapesVisible(i, false);
		        renderer.setSeriesLinesVisible(i, true);
        	}
        	else{
        		renderer.setSeriesShape( i, new Rectangle2D.Double( -1.0, -1.0, 1.0, 1.0 ) );
        		renderer.setSeriesLinesVisible(i, false);
        	}
        }
        
        //Add the renderer to the plot
        plot.setRenderer(renderer);
        
        //Only display integer values on the range axis
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
                
        //Format the display date for the x-axis
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(sdf);
        axis.setVerticalTickLabels(true);
        
        return chart;
        
    }
	
	/**
	 * Generates a plot of outdoor, indoor, thermostat temperature values
	 * using the RC that is passed to this function 
	 */
	public void plotFakeData(){				
		//Create the chart object
		JFreeChart chart = createChart(getDataset());
		//Create the panel object
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		
		setContentPane(chartPanel);
		pack();
		
		RefineryUtilities.centerFrameOnScreen(this);
		
		//Plot the chart
		this.setVisible(true);	
		
	}

}
