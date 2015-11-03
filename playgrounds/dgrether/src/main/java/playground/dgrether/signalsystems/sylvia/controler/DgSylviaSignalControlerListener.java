/* *********************************************************************** *
 * project: org.matsim.*
 * DgSylviaSignalControlerListener
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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
package playground.dgrether.signalsystems.sylvia.controler;

import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.lanes.data.v20.LaneDefinitions20;
import org.matsim.signalsystems.builder.DefaultSignalModelFactory;
import org.matsim.signalsystems.builder.FromDataBuilder;
import org.matsim.signalsystems.controler.SignalsControllerListener;
import org.matsim.signalsystems.mobsim.QSimSignalEngine;
import org.matsim.signalsystems.mobsim.SignalEngine;
import org.matsim.signalsystems.model.SignalSystemsManager;

import playground.dgrether.signalsystems.DgSensorManager;
import playground.dgrether.signalsystems.sylvia.model.DgSylviaSignalModelFactory;


/**
 * @author dgrether
 *
 */
public class DgSylviaSignalControlerListener implements SignalsControllerListener , StartupListener, IterationStartsListener,
		ShutdownListener {

	private SignalSystemsManager signalManager;
	private DgSensorManager sensorManager;
	private DgSylviaConfig sylviaConfig;
	
	public DgSylviaSignalControlerListener(DgSylviaConfig sylviaConfig) {
		this.sylviaConfig = sylviaConfig;
	}


	@Override
	public void notifyStartup(StartupEvent event) {
		ScenarioImpl scenario = (ScenarioImpl) event.getControler().getScenario();
		
		this.sensorManager = new DgSensorManager(event.getControler().getScenario().getNetwork());
		if (scenario.getConfig().scenario().isUseLanes()){
			this.sensorManager.setLaneDefinitions(scenario.getScenarioElement(LaneDefinitions20.class));
		}
		event.getControler().getEvents().addHandler(sensorManager);
		
		FromDataBuilder modelBuilder = new FromDataBuilder(scenario, 
				new DgSylviaSignalModelFactory(new DefaultSignalModelFactory(), sensorManager, this.sylviaConfig) , event.getControler().getEvents());
		this.signalManager = modelBuilder.createAndInitializeSignalSystemsManager();
		
		SignalEngine engine = new QSimSignalEngine(this.signalManager);
		event.getControler().getMobsimListeners().add(engine);
	}


	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		this.signalManager.resetModel(event.getIteration());
		this.sensorManager.reset(event.getIteration());
	}

	@Override
	public void notifyShutdown(ShutdownEvent event) {

	}

	
	
}