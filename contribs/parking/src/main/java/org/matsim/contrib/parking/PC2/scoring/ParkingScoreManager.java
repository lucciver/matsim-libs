/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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
package org.matsim.contrib.parking.PC2.scoring;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.multimodal.router.util.WalkTravelTime;
import org.matsim.contrib.parking.PC2.infrastructure.Parking;
import org.matsim.contrib.parking.lib.GeneralLib;
import org.matsim.contrib.parking.lib.obj.DoubleValueHashMap;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;

public class ParkingScoreManager {

	private ParkingBetas parkingBetas;
	private double parkingScoreScalingFactor;
	private double randomErrorTermScalingFactor;
	// TODO: also add implementation of random error term + scaling here
	DoubleValueHashMap<Id> scores;
	Controler controler;
	private WalkTravelTime walkTravelTime;
	
	
	public ParkingScoreManager(WalkTravelTime walkTravelTime) {
		this.walkTravelTime = walkTravelTime;
	}



	public double calcWalkScore(Coord destCoord, Coord parkingCoord, Id personId, double parkingDurationInSeconds){
		Map<Id,? extends Person> persons = controler.getPopulation().getPersons();
		PersonImpl person=(PersonImpl) persons.get(personId);
		
		double parkingWalkBeta = getParkingBetas().getParkingWalkBeta(person, parkingDurationInSeconds);
		
		
		
		Link link = ((NetworkImpl) controler.getNetwork()).getNearestLink(destCoord);
		double length = link.getLength();
		double walkTime = walkTravelTime.getLinkTravelTime(link, 0, person, null);
		double walkSpeed = length / walkTime;
		
		//protected double walkSpeed = 3.0 / 3.6; // [m/s]
		
		
		double walkDistance = GeneralLib.getDistance(destCoord, parkingCoord);
		double walkDurationInSeconds = walkDistance / walkSpeed;
		
		double walkingTimeTotalInMinutes=walkDurationInSeconds/60;
		
		return (parkingWalkBeta*walkingTimeTotalInMinutes)*parkingScoreScalingFactor;
	}

	public double calcCostScore(double arrivalTime, double parkingDurationInSeconds, Parking parking, Id personId){
		Map<Id,? extends Person> persons = controler.getPopulation().getPersons();
		PersonImpl person=(PersonImpl) persons.get(personId);
		double parkingCostBeta = getParkingBetas().getParkingCostBeta(person);
		
		double parkingCost=parking.getCost(personId, arrivalTime, parkingDurationInSeconds);
		
		return (parkingCostBeta*parkingCost)*parkingScoreScalingFactor;
	}


	public double calcScore(Coord destCoord, double arrivalTime, double parkingDurationInSeconds, Parking parking, Id personId){
		double walkScore=calcWalkScore(destCoord, parking.getCoordinate(), personId,parkingDurationInSeconds);
		double costScore=calcCostScore(arrivalTime, parkingDurationInSeconds, parking, personId);
		return costScore + walkScore;
	}
	
	
	
	
	
	
	
	public double getScore(Id id) {
		return scores.get(id);
	}
	
	public void addScore(Id id, double incValue) {
		scores.incrementBy(id, incValue);
	}
	
	
	public void prepareForNewIteration(){
		scores=new DoubleValueHashMap<Id>();
	}







	public double getParkingScoreScalingFactor() {
		return parkingScoreScalingFactor;
	}







	public void setParkingScoreScalingFactor(double parkingScoreScalingFactor) {
		this.parkingScoreScalingFactor = parkingScoreScalingFactor;
	}







	public double getRandomErrorTermScalingFactor() {
		return randomErrorTermScalingFactor;
	}







	public void setRandomErrorTermScalingFactor(double randomErrorTermScalingFactor) {
		this.randomErrorTermScalingFactor = randomErrorTermScalingFactor;
	}







	public ParkingBetas getParkingBetas() {
		return parkingBetas;
	}







	public void setParkingBetas(ParkingBetas parkingBetas) {
		this.parkingBetas = parkingBetas;
	}

	

}
