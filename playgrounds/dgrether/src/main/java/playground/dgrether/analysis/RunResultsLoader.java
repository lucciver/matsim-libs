/* *********************************************************************** *
 * project: org.matsim.*
 * RunDirectoryLoader
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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
package playground.dgrether.analysis;

import java.io.File;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.lanes.data.MatsimLaneDefinitionsReader;
import org.matsim.lanes.data.v20.LaneDefinitions20;
import org.matsim.lanes.data.v20.LaneDefinitionsReader20;
import org.matsim.signalsystems.data.SignalsData;
import org.matsim.signalsystems.data.SignalsScenarioLoader;
import org.matsim.signalsystems.data.SignalsScenarioWriter;
import org.matsim.signalsystems.data.signalsystems.v20.SignalSystemsData;

import playground.dgrether.signalsystems.utils.DgScenarioUtils;


/**
 * 
 * @author dgrether
 *
 */
public class RunResultsLoader {
	
	private String directory;
	private String runId;
	private OutputDirectoryHierarchy outputDir;
	private Network network;
	private Population population;
	private LaneDefinitions20 lanes;
	private SignalsData signals;
	
	public RunResultsLoader(String path, String runId) {
		this.directory = path;
		this.runId = runId;
		initialize();
	}
	
	private void initialize(){
		File dir = new File(this.directory);
		if (! (dir.exists() && dir.isDirectory())) {
			throw new IllegalArgumentException("Run directory " + this.directory + " can not be found");
		}
		this.outputDir = new OutputDirectoryHierarchy(this.directory, this.runId, false, false);
		String configFilename = outputDir.getOutputFilename(Controler.FILENAME_CONFIG);
	}

	public String getEventsFilename(Integer iteration){
		return this.outputDir.getIterationFilename(iteration, Controler.FILENAME_EVENTS_XML);
	}
	
	public Network getNetwork(){
		if (this.network == null) {
			String nf = this.outputDir.getOutputFilename(Controler.FILENAME_NETWORK);
			this.network = loadNetwork(nf);
		}
		return this.network;
	}

	private Network loadNetwork(String path) {
		//Why we have to do all this, we simply want to read a file
		Config c = ConfigUtils.createConfig(); 
		c.network().setInputFile(path);
		Scenario sc = ScenarioUtils.createScenario(c);
		MatsimNetworkReader nr = new MatsimNetworkReader(sc);
		nr.readFile(path);
		return sc.getNetwork();
	}
	
	public Population getPopulation(){
		if (this.population == null) {
			String pf = this.outputDir.getOutputFilename(Controler.FILENAME_POPULATION);
			this.population = this.loadPopulation(pf);
		}
		return this.population;
	}

	private Population loadPopulation(String path) {
		//Why we have to do all this, we simply want to read a file
		Config c = ConfigUtils.createConfig(); 
		c.plans().setInputFile(path);
		Scenario sc = ScenarioUtils.createScenario(c);
		MatsimPopulationReader pr= new MatsimPopulationReader(sc);
		pr.readFile(path);
		return sc.getPopulation();
	}
	
	//untested
	public LaneDefinitions20 getLanes() {
		if (this.lanes == null){
			String lf = this.outputDir.getOutputFilename(Controler.FILENAME_LANES);
			this.lanes = this.loadLanes(lf);
		}
		return this.lanes;
	}
	
	private LaneDefinitions20 loadLanes(String path) {
		Config c = ConfigUtils.createConfig();
		c.scenario().setUseLanes(true);
		Scenario sc = ScenarioUtils.createScenario(c);
		MatsimLaneDefinitionsReader reader = new MatsimLaneDefinitionsReader(sc);
		reader.readFile(path);
		return (LaneDefinitions20) sc.getScenarioElement(LaneDefinitions20.ELEMENT_NAME);
	}
	
	public SignalsData getSignals() {
		if (this.signals == null) {
			String systemsfile = this.outputDir.getOutputFilename(SignalsScenarioWriter.FILENAME_SIGNAL_SYSTEMS );
			String groupsfile = this.outputDir.getOutputFilename(SignalsScenarioWriter.FILENAME_SIGNAL_GROUPS);
			String controlfile = this.outputDir.getOutputFilename(SignalsScenarioWriter.FILENAME_SIGNAL_CONTROL);
			this.signals = loadSignals(systemsfile, groupsfile, controlfile);
		}
		return this.signals;
	}

	private SignalsData loadSignals(String systemspath, String groupspath, String controlpath) {
		Config c = ConfigUtils.createConfig(); 
		c.signalSystems().setSignalSystemFile(systemspath);
		c.signalSystems().setSignalGroupsFile(groupspath);
		c.signalSystems().setSignalControlFile(controlpath);
		SignalsScenarioLoader loader = new SignalsScenarioLoader(c.signalSystems());
		return loader.loadSignalsData();
	}

	public final String getIterationPath(int iteration) {
		return outputDir.getIterationPath(iteration);
	}

	public final String getIterationFilename(int iteration, String filename) {
		return outputDir.getIterationFilename(iteration, filename);
	}

	public final String getOutputFilename(String filename) {
		return outputDir.getOutputFilename(filename);
	}

	public String getOutputPath() {
		return outputDir.getOutputPath();
	}
	
	
	
}
