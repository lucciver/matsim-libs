/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package playground.ikaddoura.optimization.io;

/**
 * 
 * @author ikaddoura
 *
 */
public class OptSettings {
	
	private double incrHeadway = 0.;
	private double incrFare = 0.;
	private int incrCapacity = 0;
	private int incrDemand = 0;
	private double startHeadway = 600;
	private double startFare = 0.;
	private int startCapacity = 100;
	private int startDemand = 0;
	private int stepsHeadway = 0;
	private int stepsFare = 0;
	private int stepsCapacity = 0;
	private int stepsDemand = 0;
	private boolean useRandomSeedsFile = false;
	private String randomSeedsFile = null;
	private boolean usePopulationPathsFile = false;
	private String populationPathsFile = null;
	private boolean marginalCostPricing = false;

	public double getIncrHeadway() {
		return incrHeadway;
	}
	public void setIncrHeadway(double incrHeadway) {
		this.incrHeadway = incrHeadway;
	}
	public double getIncrFare() {
		return incrFare;
	}
	public void setIncrFare(double incrFare) {
		this.incrFare = incrFare;
	}
	public int getIncrCapacity() {
		return incrCapacity;
	}
	public void setIncrCapacity(int incrCapacity) {
		this.incrCapacity = incrCapacity;
	}
	public double getStartHeadway() {
		return startHeadway;
	}
	public void setStartHeadway(double startHeadway) {
		this.startHeadway = startHeadway;
	}
	public double getStartFare() {
		return startFare;
	}
	public void setStartFare(double startFare) {
		this.startFare = startFare;
	}
	public int getStartCapacity() {
		return startCapacity;
	}
	public void setStartCapacity(int startCapacity) {
		this.startCapacity = startCapacity;
	}
	public int getStepsHeadway() {
		return stepsHeadway;
	}
	public void setStepsHeadway(int stepsHeadway) {
		this.stepsHeadway = stepsHeadway;
	}
	public int getStepsFare() {
		return stepsFare;
	}
	public void setStepsFare(int stepsFare) {
		this.stepsFare = stepsFare;
	}
	public int getStepsCapacity() {
		return stepsCapacity;
	}
	public void setStepsCapacity(int stepsCapacity) {
		this.stepsCapacity = stepsCapacity;
	}
	public int getIncrDemand() {
		return incrDemand;
	}
	public void setIncrDemand(int incrDemand) {
		this.incrDemand = incrDemand;
	}
	public int getStartDemand() {
		return startDemand;
	}
	public void setStartDemand(int startDemand) {
		this.startDemand = startDemand;
	}
	public int getStepsDemand() {
		return stepsDemand;
	}
	public void setStepsDemand(int stepsDemand) {
		this.stepsDemand = stepsDemand;
	}
	public boolean isUseRandomSeedsFile() {
		return useRandomSeedsFile;
	}
	public void setUseRandomSeedsFile(boolean useRandomSeedsFile) {
		this.useRandomSeedsFile = useRandomSeedsFile;
	}
	public String getRandomSeedsFile() {
		return randomSeedsFile;
	}
	public void setRandomSeedsFile(String randomSeedsFile) {
		this.randomSeedsFile = randomSeedsFile;
	}
	public boolean isUsePopulationPathsFile() {
		return usePopulationPathsFile;
	}
	public void setUsePopulationPathsFile(boolean usePopulationPathsFile) {
		this.usePopulationPathsFile = usePopulationPathsFile;
	}
	public String getPopulationPathsFile() {
		return populationPathsFile;
	}
	public void setPopulationPathsFile(String populationPathsFile) {
		this.populationPathsFile = populationPathsFile;
	}
	public boolean isMarginalCostPricing() {
		return marginalCostPricing;
	}
	public void setMarginalCostPricing(boolean marginalCostPricing) {
		this.marginalCostPricing = marginalCostPricing;
	}
}