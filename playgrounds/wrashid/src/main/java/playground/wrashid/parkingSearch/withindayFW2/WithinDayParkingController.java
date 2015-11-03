/* *********************************************************************** *
 * project: org.matsim.*
 * WithinDayParkingController.java
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

package playground.wrashid.parkingSearch.withindayFW2;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.controler.events.ReplanningEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.ReplanningListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.mobsim.framework.MobsimFactory;
import org.matsim.core.mobsim.qsim.multimodalsimengine.router.util.BikeTravelTime;
import org.matsim.core.mobsim.qsim.multimodalsimengine.router.util.PTTravelTime;
import org.matsim.core.mobsim.qsim.multimodalsimengine.router.util.RideTravelTime;
import org.matsim.core.mobsim.qsim.multimodalsimengine.router.util.WalkTravelTime;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.population.PopulationFactoryImpl;
import org.matsim.core.population.routes.ModeRouteFactory;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeAndDisutility;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelCostCalculatorFactory;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.AStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelTime;
import org.matsim.facilities.algorithms.WorldConnectLocations;
import org.matsim.withinday.controller.WithinDayController;
import org.matsim.withinday.replanning.modules.ReplanningModule;

import playground.wrashid.lib.obj.IntegerValueHashMap;
import playground.wrashid.parkingSearch.withinday.LegModeChecker;
import playground.wrashid.parkingSearch.withindayFW.core.InsertParkingActivities;
import playground.wrashid.parkingSearch.withindayFW.core.ParkingInfrastructure;
import playground.wrashid.parkingSearch.withindayFW.core.mobsim.ParkingQSimFactory;

public class WithinDayParkingController extends WithinDayController implements StartupListener, ReplanningListener {

	/*
	 * How many parallel Threads shall do the Replanning.
	 */
	protected int numReplanningThreads = 8;

	protected ParkingSearchIdentifier randomSearchIdentifier;
	protected ParkingSearchReplannerFactory randomSearchReplannerFactory;

	protected LegModeChecker legModeChecker;
	protected ParkingAgentsTracker parkingAgentsTracker;
	protected InsertParkingActivities insertParkingActivities;
	protected ParkingInfrastructure parkingInfrastructure;
	
	public WithinDayParkingController(String[] args) {
		super(args);
		
		// register this as a Controller Listener
		super.addControlerListener(this);
	}

	protected void initIdentifiers() {

		this.randomSearchIdentifier = new ParkingSearchIdentifier(parkingAgentsTracker, parkingInfrastructure); 
		this.getFixedOrderSimulationListener().addSimulationListener(this.randomSearchIdentifier);
	}
	
	/*
	 * New Routers for the Replanning are used instead of using the controler's.
	 * By doing this every person can use a personalised Router.
	 */
	protected void initReplanners() {

		LeastCostPathCalculatorFactory factory = new AStarLandmarksFactory(this.network, new FreespeedTravelTimeAndDisutility(this.config.planCalcScore()));
		ModeRouteFactory routeFactory = ((PopulationFactoryImpl) this.scenarioData.getPopulation().getFactory()).getModeRouteFactory();

		// create a copy of the MultiModalTravelTimeWrapperFactory and set the TravelTimeCollector for car mode
		Map<String, TravelTime> times = new HashMap<String, TravelTime>();
		times.put(TransportMode.walk, new WalkTravelTime(this.config.plansCalcRoute()));
		times.put(TransportMode.bike, new BikeTravelTime(this.config.plansCalcRoute(),
				new WalkTravelTime(this.config.plansCalcRoute())));
		times.put(TransportMode.ride, new RideTravelTime(this.getLinkTravelTimes(), 
				new WalkTravelTime(this.config.plansCalcRoute())));
		times.put(TransportMode.pt, new PTTravelTime(this.config.plansCalcRoute(), 
				this.getLinkTravelTimes(), new WalkTravelTime(this.config.plansCalcRoute())));
		times.put(TransportMode.car, super.getTravelTimeCollector());
		
		TravelDisutilityFactory costFactory = new OnlyTimeDependentTravelCostCalculatorFactory();
		
		AbstractMultithreadedModule router = new ReplanningModule(config, network, costFactory, times, factory, routeFactory);
	
		this.randomSearchReplannerFactory = new ParkingSearchReplannerFactory(this.getWithinDayEngine(), router, 1.0, this.scenarioData, parkingAgentsTracker);
		this.randomSearchReplannerFactory.addIdentifier(this.randomSearchIdentifier);		
		this.getWithinDayEngine().addDuringLegReplannerFactory(this.randomSearchReplannerFactory);
	}
	
	protected void setUp() {
		super.setUp();
		
		
// connect facilities to network
		new WorldConnectLocations(this.config).connectFacilitiesWithLinks(getFacilities(), (NetworkImpl) getNetwork());
		
		super.initWithinDayEngine(numReplanningThreads);
		super.createAndInitTravelTimeCollector();
		super.createAndInitLinkReplanningMap();
		
// ensure that all agents' plans have valid mode chains
		legModeChecker = new LegModeChecker(this.scenarioData, this.createRoutingAlgorithm());
		legModeChecker.setValidNonCarModes(new String[]{TransportMode.walk});
		legModeChecker.setToCarProbability(0.5);
		legModeChecker.run(this.scenarioData.getPopulation());
		
		parkingInfrastructure = new ParkingInfrastructure(this.scenarioData,null,null);
		
		// init parking facility capacities
				IntegerValueHashMap<Id> facilityCapacities = new IntegerValueHashMap<Id>();
				parkingInfrastructure.setFacilityCapacities(facilityCapacities);
				for (ActivityFacility parkingFacility : parkingInfrastructure.getParkingFacilities()) {
					facilityCapacities.incrementBy(parkingFacility.getId(), 10);
				}
		
		
//		Set<Id> parkingFacilityIds = parkingInfrastructure.getFacilityCapacities().getKeySet();
//		for (Id id : parkingFacilityIds) parkingInfrastructure.getFacilityCapacities().incrementBy(id, 1000);
		
		parkingAgentsTracker = new ParkingAgentsTracker(this.scenarioData, 2000.0);
		this.getFixedOrderSimulationListener().addSimulationListener(this.parkingAgentsTracker);
		this.getEvents().addHandler(this.parkingAgentsTracker);
		
		insertParkingActivities = new InsertParkingActivities(scenarioData, this.createRoutingAlgorithm(), parkingInfrastructure);
		
		MobsimFactory mobsimFactory = new ParkingQSimFactory(insertParkingActivities, parkingInfrastructure, this.getWithinDayEngine());
		this.setMobsimFactory(mobsimFactory);
		
		this.initIdentifiers();
		this.initReplanners();
	}
	
	/*
	 * When the Controller Startup Event is created, the EventsManager
	 * has already been initialized. Therefore we can initialize now
	 * all Objects, that have to be registered at the EventsManager.
	 */
	@Override
	public void notifyStartup(StartupEvent event) {

	}

	@Override
	public void notifyReplanning(ReplanningEvent event) {
		/*
		 * During the replanning the mode chain of the agents' selected plans
		 * might have been changed. Therefore, we have to ensure that the 
		 * chains are still valid.
		 */
		for (Person person : this.scenarioData.getPopulation().getPersons().values()) {
			legModeChecker.run(person.getSelectedPlan());			
		}
	}
		
	/*
	 * ===================================================================
	 * main
	 * ===================================================================
	 */
	public static void main(String[] args) {
		if ((args == null) || (args.length == 0)) {
			System.out.println("No argument given!");
			System.out.println("Usage: Controler config-file [dtd-file]");
			System.out.println("using default config");
			args=new String[]{"test/input/playground/wrashid/parkingSearch/withinday/chessboard/config_plans1.xml"};
//			args=new String[]{"test/input/playground/wrashid/parkingSearch/withinday/chessboard/config.xml"};
		}
		final WithinDayParkingController controller = new WithinDayParkingController(args);
		controller.setOverwriteFiles(true);
		
		
		controller.run();
		
		System.exit(0);
	}
}