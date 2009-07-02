/* *********************************************************************** *
 * project: org.matsim.*
 * AverageEventsToGAP.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package playground.jjoubert.CommercialModel.Postprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import playground.jjoubert.DateString;

public class AverageEventsToGAP {

	private final static String ROOT = "/Users/johanwjoubert/MATSim/workspace/MATSimData/Commercial/PostProcess/";
	private final static String FOLDER = ROOT + "Run";
	private final static String[] RUNS = {"01","02","03","04"};

	/**
	 * This class reads in multiple files containing the normalized values of simulated minor 
	 * commercial activities for each of the zones (GAP mesozones in the South African 
	 * implementation). The averages are then calculated, and written out to file.
	 * 
	 * @param args
	 */

	public static void main(String args[]){
		System.out.printf("==========================================================================================\n");
		System.out.printf("  Averaging the normalized simulated GAP activity densities for multiple runs.\n");
		System.out.printf("==========================================================================================\n\n");

		/*
		 * Start by creating an empty list of all possible zones, based on the zone IDs. Here I've worked with
		 * the GAP mesozones.
		 */
		int maxZones = 30000;
		ArrayList<ArrayList<Double>> zoneList = new ArrayList<ArrayList<Double>>(maxZones+1);
		for(int i = 0; i < maxZones+1; i++){
			/*
			 * Use the 0th index to indicate if the specific zone has been set. Values of '1' indicate a set zone.
			 */
			ArrayList<Double> hours = new ArrayList<Double>(25);
			hours.add(0.0);
			for(int j = 0; j < 24; j++){
				Double value = 0.0;
				hours.add(value);				
			}
			zoneList.add(hours);
		}
		/*
		 * To keep the sequence of zone the same as they are read in, I need to keep a sequential list
		 * of them as they are read in.
		 */
		ArrayList<Integer> zoneSequence = new ArrayList<Integer>();
		boolean zoneSet = false;

		String header = null;
		for (String run : RUNS) {
			String folderName = FOLDER + run + "/";
			File folder = new File(folderName);
			if(!folder.isDirectory()){
				System.err.printf("%s is not a directory!\n", folderName);
				System.exit(0);
			} else{
				System.out.printf("   Reading %s... ", folder.getPath());
				File[] fileList = folder.listFiles();
				for (File file : fileList) {
					if(file.getName().length() > 20 && 
							file.getName().startsWith("SimulatedCommercialMinorGAP_Normalized")){
						try {
							Scanner input = new Scanner(new BufferedReader(new FileReader( file )));
							header = input.nextLine();
							while(input.hasNextLine()){
								String[] line = input.nextLine().split(",");
								Integer zoneId = Integer.parseInt(line[0]);
								if(zoneId == 3579){
									System.out.printf("");
								}
								if(!zoneSet){
									zoneSequence.add(zoneId);
								}
								zoneList.get(zoneId).set(0, 1.);
								for(int i = 1; i < line.length; i++){
									Double oldValue = zoneList.get(zoneId).get(i);
									zoneList.get(zoneId).set(i, oldValue + Double.parseDouble(line[i]));
								}
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						zoneSet = true;
					}
				}
				System.out.printf("Done.\n");
			}
		}
		/*
		 * Calculate the averages.
		 */
		System.out.printf("   Calculating averages... ");
		Double number = (double) RUNS.length;
		for (Integer z1 : zoneSequence) {
			ArrayList<Double> theZone = zoneList.get(z1);
			if(theZone.get(0) == 0){
				System.err.printf("Trying to calculate averages for out zone %d that has not been set!", z1);
				System.exit(0);
			} else{
				for(int i = 1; i < theZone.size(); i++){
					double oldValue = theZone.get(i);
					theZone.set(i, (oldValue/number) );
				}
			}
		}
		System.out.printf("Done.\n");

		/*
		 * Write the averages of the zone activity densities to file.
		 */
		System.out.printf("   Writing the average densities to file... ");
		DateString ds = new DateString();
		String outputFileName = ROOT + "AverageSimulatedCommercialMinorGAP_Normalized_" + ds.toString() + ".txt";
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(new File(outputFileName)));
			try{
				output.write(header);
				output.newLine();
				for (Integer z2 : zoneSequence) {
					ArrayList<Double> theZone = zoneList.get(z2);
					if(theZone.get(0) == 0){
						System.err.printf("Trying to write out zone %d that has not been set!", z2);
						System.exit(0);					
					} else{
						output.write(String.valueOf(z2));
						output.write(",");
						for(int j = 1; j < theZone.size()-1; j++) {
							output.write(String.valueOf(theZone.get(j)));
							output.write(",");
						}
						output.write(String.valueOf(theZone.get(theZone.size()-1)));
						output.newLine();
					}
				}
			} finally{
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done.");

		System.out.println("Completed successfully!");
	}
	
}