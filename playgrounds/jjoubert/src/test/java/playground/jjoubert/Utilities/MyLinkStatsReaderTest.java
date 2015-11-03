/* *********************************************************************** *
 * project: org.matsim.*
 * MyLinkStatsReaderTest.java
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

package playground.jjoubert.Utilities;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.TravelTimeData;
import org.matsim.core.config.ConfigUtils;
import org.matsim.testcases.MatsimTestCase;

import playground.jjoubert.Utilities.matsim2urbansim.MyLinkStatsReader;


public class MyLinkStatsReaderTest extends MatsimTestCase{
	private Logger log = Logger.getLogger(MyLinkStatsReaderTest.class);
	private Scenario scenario;
	
	/**
	 * The linkstats.txt file was generated by running the equil example for
	 * 10 iterations using the example's own config file; the plans100.xml
	 * file; and the given network.xml file.
	 */
	public void testMyLinkstatsReaderConstructor(){
		String f1 = (new File(getInputDirectory())).getParent() + "/dummy.txt";
		try{
			MyLinkStatsReader mlsr1 = new MyLinkStatsReader(f1);
			fail("Uncaught exception: dummy file " + f1 + " does not exist.");
		} catch (Exception e) {
			log.info("Caught expected exception.");
		}
		
		String f2 = (new File(getInputDirectory())).getParent() + "/linkstats.txt";
		try{
			MyLinkStatsReader mlsr2 = new MyLinkStatsReader(f2);
		} catch (Exception e) {
			fail("Should not have thrown exception: " + f2 + " file exist.");
		}
	}
	
	/**
	 * Read the average travel time from 6-7, and check three links.
	 */
	public void testReadSingleHour(){
		String f = (new File(getInputDirectory())).getParent() + "/linkstats.txt";
		MyLinkStatsReader m = new MyLinkStatsReader(f);
		Map<Id,Double> map = m.readSingleHour("6-7");
		assertEquals("Wrong travel time read for link 10.", "361.67", String.format("%3.2f", map.get(new IdImpl("10"))));
		assertEquals("Wrong travel time read for link 18.", "192.33", String.format("%3.2f", map.get(new IdImpl("18"))));
		assertEquals("Wrong travel time read for link 22.", "1259.90", String.format("%4.2f", map.get(new IdImpl("22"))));
	}
	
	/**
	 * Read the entire linkstats file, and check a few entries.
	 */
	public void testBuildTravelTimeDataObjects(){
		setupTest();
		String f = (new File(getInputDirectory())).getParent() + "/linkstats.txt";
		MyLinkStatsReader m = new MyLinkStatsReader(f);
		Map<Id,TravelTimeData> map1 = m.buildTravelTimeDataObject(scenario, "min");
		Map<Id,TravelTimeData> map2 = m.buildTravelTimeDataObject(scenario, "avg");
		Map<Id,TravelTimeData> map3 = m.buildTravelTimeDataObject(scenario, "max");
		
		assertEquals("Wrong entry for link 2; 6-7; min.", "360.44", String.format("%3.2f", map1.get(new IdImpl("2")).getTravelTime(6, EPSILON)));
		assertEquals("Wrong entry for link 2; 6-7; avg.", "360.65", String.format("%3.2f", map2.get(new IdImpl("2")).getTravelTime(6, EPSILON)));
		assertEquals("Wrong entry for link 2; 6-7; max.", "360.70", String.format("%3.2f", map3.get(new IdImpl("2")).getTravelTime(6, EPSILON)));

		assertEquals("Wrong entry for link 22; 9-10; max.", "1259.90", String.format("%4.2f", map3.get(new IdImpl("22")).getTravelTime(9, EPSILON)));
		assertEquals("Wrong entry for link 22; 10-11; min.", "1260.00", String.format("%4.2f", map1.get(new IdImpl("22")).getTravelTime(10, EPSILON)));
		
		assertEquals("Wrong entry for link 12; 6-7; min.", "183.60", String.format("%3.2f", map1.get(new IdImpl("12")).getTravelTime(6, EPSILON)));
		assertEquals("Wrong entry for link 12; 6-7; avg.", "185.43", String.format("%3.2f", map2.get(new IdImpl("12")).getTravelTime(6, EPSILON)));
		assertEquals("Wrong entry for link 12; 6-7; max.", "187.00", String.format("%3.2f", map3.get(new IdImpl("12")).getTravelTime(6, EPSILON)));
}	


	private void setupTest() {
		scenario = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		String f = (new File(getInputDirectory())).getParent() + "/network.xml";
		MatsimNetworkReader mnr = new MatsimNetworkReader(scenario);
		mnr.readFile(f);
	}

}