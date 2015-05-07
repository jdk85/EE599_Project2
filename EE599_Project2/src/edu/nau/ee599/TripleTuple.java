package edu.nau.ee599;
public class TripleTuple{
	
	private long timestamp;
	private double temperature;
	private double estimatedTemperature;
	/**
	 * Constructor - if estimatedTemperature doesn't exist yet we pass a Double.NaN
	 * @param timestamp
	 * @param temperature
	 * @param estimatedTemperature
	 */
	public TripleTuple(long timestamp, double temperature,double estimatedTemperature){
		setTimestamp(timestamp);
		setTemperature(temperature);
		setEstimatedTemperature(estimatedTemperature);
	}
	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return the temperature
	 */
	public double getTemperature() {
		return temperature;
	}
	/**
	 * @param temperature the temperature to set
	 */
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	/**
	 * @return the estimatedTemperature
	 */
	public double getEstimatedTemperature() {
		return estimatedTemperature;
	}
	/**
	 * @param estimatedTemperature the estimatedTemperature to set
	 */
	public void setEstimatedTemperature(double estimatedTemperature) {
		this.estimatedTemperature = estimatedTemperature;
	}

}