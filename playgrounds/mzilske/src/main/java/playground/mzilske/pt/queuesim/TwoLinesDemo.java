///* *********************************************************************** *
// * project: org.matsim.*
// * TwoLinesDemo.java
// *                                                                         *
// * *********************************************************************** *
// *                                                                         *
// * copyright       : (C) 2009 by the members listed in the COPYING,        *
// *                   LICENSE and WARRANTY file.                            *
// * email           : info at matsim dot org                                *
// *                                                                         *
// * *********************************************************************** *
// *                                                                         *
// *   This program is free software; you can redistribute it and/or modify  *
// *   it under the terms of the GNU General Public License as published by  *
// *   the Free Software Foundation; either version 2 of the License, or     *
// *   (at your option) any later version.                                   *
// *   See also COPYING, LICENSE and WARRANTY file                           *
// *                                                                         *
// * *********************************************************************** */
//
//package playground.mzilske.pt.queuesim;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import org.matsim.api.core.v01.Id;
//import org.matsim.api.core.v01.ScenarioImpl;
//import org.matsim.api.core.v01.TransportMode;
//import org.matsim.api.core.v01.network.Link;
//import org.matsim.api.core.v01.network.Node;
//import org.matsim.api.core.v01.population.PopulationFactory;
//import org.matsim.core.basic.v01.IdImpl;
//import org.matsim.core.config.Config;
//import org.matsim.core.events.EventsManagerImpl;
//import org.matsim.core.network.LinkImpl;
//import org.matsim.core.network.NetworkLayer;
//import org.matsim.core.population.ActivityImpl;
//import org.matsim.core.population.LegImpl;
//import org.matsim.core.population.PersonImpl;
//import org.matsim.core.population.PlanImpl;
//import org.matsim.core.population.PopulationImpl;
//import org.matsim.core.population.routes.NetworkRouteWRefs;
//import org.matsim.core.utils.misc.Time;
//import org.matsim.pt.Umlauf;
//import org.matsim.pt.UmlaufInterpolator;
//import org.matsim.pt.queuesim.TransitQueueSimulation;
//import org.matsim.pt.routes.ExperimentalTransitRoute;
//import org.matsim.transitSchedule.api.TransitLine;
//import org.matsim.transitSchedule.api.TransitRoute;
//import org.matsim.transitSchedule.api.TransitRouteStop;
//import org.matsim.transitSchedule.api.TransitSchedule;
//import org.matsim.transitSchedule.api.TransitScheduleFactory;
//import org.matsim.transitSchedule.api.TransitStopFacility;
//import org.matsim.vehicles.BasicVehicle;
//import org.matsim.vehicles.BasicVehicleCapacity;
//import org.matsim.vehicles.BasicVehicleCapacityImpl;
//import org.matsim.vehicles.BasicVehicleType;
//import org.matsim.vehicles.VehiclesFactory;
//
//import playground.mrieser.pt.analysis.RouteOccupancy;
//import playground.mrieser.pt.analysis.VehicleTracker;
//import playground.mzilske.bvg09.OTFDemo;
//
//public class TwoLinesDemo {
//
//	private final ScenarioImpl scenario = new ScenarioImpl();
//	private final Id[] ids = new Id[30];
//
//	private void createIds() {
//		for (int i = 0; i < this.ids.length; i++) {
//			this.ids[i] = this.scenario.createId(Integer.toString(i));
//		}
//	}
//
//	private void prepareConfig() {
//		Config config = this.scenario.getConfig();
//		config.scenario().setUseTransit(true);
//		config.scenario().setUseVehicles(true);
//		config.simulation().setSnapshotStyle("queue");
//		config.simulation().setEndTime(24.0*3600);
//	}
//
//	private void createNetwork() {
//		/*                   
//		 * (2)-2 21--(4)-4 20--(6)                                               (12)---12-17--(14)
//		 *                    o  \                                               /o
//		 *                    2   \                                             /  6
//		 *                         6 19                                       10 18
//		 *                          \   8                         7           /
//		 *                           \  o                         o          /
//		 *                           (7)---7- 16--(8)---8- 15--(9)---9 14--(10)
//		 *                           /        o                           o  \
//		 *                          /         3                           4   \
//		 *                         5 24                                       11
//		 *                        /                                             \ 23
//		 *                       /                                              o\
//		 * (1)---1-26(3)---3-25-(5)                                             5 (11)---13-22-(13)
//		 *                    o
//		 *                    1
//		 *
//		 */
//		NetworkLayer network = this.scenario.getNetwork();
//		network.setCapacityPeriod(3600.0);
//		Node node1 = network.createAndAddNode(this.ids[1], this.scenario.createCoord(-2000, 0));
//		Node node2 = network.createAndAddNode(this.ids[2], this.scenario.createCoord(-2000, 1000));
//		Node node3 = network.createAndAddNode(this.ids[3], this.scenario.createCoord(-1000, 0));
//		Node node4 = network.createAndAddNode(this.ids[4], this.scenario.createCoord(-1000, 1000));
//		Node node5 = network.createAndAddNode(this.ids[5], this.scenario.createCoord(0, 0));
//		Node node6 = network.createAndAddNode(this.ids[6], this.scenario.createCoord(0, 1000));
//		Node node7 = network.createAndAddNode(this.ids[7], this.scenario.createCoord(500, 500));
//		Node node8 = network.createAndAddNode(this.ids[8], this.scenario.createCoord(1500, 500));
//		Node node9 = network.createAndAddNode(this.ids[9], this.scenario.createCoord(2500, 500));
//		Node node10 = network.createAndAddNode(this.ids[10], this.scenario.createCoord(3500, 500));
//		Node node11 = network.createAndAddNode(this.ids[11], this.scenario.createCoord(4000, 0));
//		Node node12 = network.createAndAddNode(this.ids[12], this.scenario.createCoord(4000, 1000));
//		Node node13 = network.createAndAddNode(this.ids[13], this.scenario.createCoord(5000, 0));
//		Node node14 = network.createAndAddNode(this.ids[14], this.scenario.createCoord(5000, 1000));
//
//		network.createAndAddLink(this.ids[1], node1, node3, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[2], node2, node4, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[3], node3, node5, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[4], node4, node6, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[5], node5, node7, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[6], node6, node7, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[7], node7, node8, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[8], node8, node9, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[9], node9, node10, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[10], node10, node12, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[11], node10, node11, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[12], node12, node14, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[13], node11, node13, 1000.0, 10.0, 3600.0, 1);
//
//		network.createAndAddLink(this.ids[14], node10, node9, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[15], node9, node8, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[16], node8, node7, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[17], node14, node12, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[18], node12, node10, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[19], node7, node6, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[20], node6, node4, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[21], node4, node2, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[22], node13, node11, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[23], node11, node10, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[24], node7, node5, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[25], node5, node3, 1000.0, 10.0, 3600.0, 1);
//		network.createAndAddLink(this.ids[26], node3, node1, 1000.0, 10.0, 3600.0, 1);
//	}
//
//	private void createTransitSchedule() {
//		TransitSchedule schedule = this.scenario.getTransitSchedule();
//		TransitScheduleFactory builder = schedule.getFactory();
//		TransitStopFacility stop1 = builder.createTransitStopFacility(this.ids[1], this.scenario.createCoord(-100, -50), false);
//		TransitStopFacility stop2 = builder.createTransitStopFacility(this.ids[2], this.scenario.createCoord(-100, 850), false);
//		TransitStopFacility stop3 = builder.createTransitStopFacility(this.ids[3], this.scenario.createCoord(1400, 450), false);
//		TransitStopFacility stop4 = builder.createTransitStopFacility(this.ids[4], this.scenario.createCoord(3400, 450), false);
//		TransitStopFacility stop5 = builder.createTransitStopFacility(this.ids[5], this.scenario.createCoord(3900, 50), false);
//		TransitStopFacility stop6 = builder.createTransitStopFacility(this.ids[6], this.scenario.createCoord(3900, 850), false);
//		
//		TransitStopFacility stop7 = builder.createTransitStopFacility(this.ids[7], this.scenario.createCoord(2600, 550), false);
//		TransitStopFacility stop8 = builder.createTransitStopFacility(this.ids[8], this.scenario.createCoord( 600, 550), false);
//
//		LinkImpl link1 = this.scenario.getNetwork().getLinks().get(this.ids[1]);
//		LinkImpl link2 = this.scenario.getNetwork().getLinks().get(this.ids[2]);
//		LinkImpl link3 = this.scenario.getNetwork().getLinks().get(this.ids[3]);
//		LinkImpl link4 = this.scenario.getNetwork().getLinks().get(this.ids[4]);
//		LinkImpl link5 = this.scenario.getNetwork().getLinks().get(this.ids[5]);
//		LinkImpl link6 = this.scenario.getNetwork().getLinks().get(this.ids[6]);
//		LinkImpl link7 = this.scenario.getNetwork().getLinks().get(this.ids[7]);
//		LinkImpl link8 = this.scenario.getNetwork().getLinks().get(this.ids[8]);
//		LinkImpl link9 = this.scenario.getNetwork().getLinks().get(this.ids[9]);
//		LinkImpl link10 = this.scenario.getNetwork().getLinks().get(this.ids[10]);
//		LinkImpl link11 = this.scenario.getNetwork().getLinks().get(this.ids[11]);
//		LinkImpl link12 = this.scenario.getNetwork().getLinks().get(this.ids[12]);
//		LinkImpl link13 = this.scenario.getNetwork().getLinks().get(this.ids[13]);
//		LinkImpl link14 = this.scenario.getNetwork().getLinks().get(this.ids[14]);
//		LinkImpl link15 = this.scenario.getNetwork().getLinks().get(this.ids[15]);
//		LinkImpl link16 = this.scenario.getNetwork().getLinks().get(this.ids[16]);
//		LinkImpl link17 = this.scenario.getNetwork().getLinks().get(this.ids[17]);
//		LinkImpl link18 = this.scenario.getNetwork().getLinks().get(this.ids[18]);
//		LinkImpl link19 = this.scenario.getNetwork().getLinks().get(this.ids[19]);
//		
//		stop1.setLink(link3);
//		stop2.setLink(link4);
//		stop3.setLink(link7);
//		stop4.setLink(link9);
//		stop5.setLink(link11);
//		stop6.setLink(link10);
//		stop7.setLink(link14);
//		stop8.setLink(link16);
//
//		schedule.addStopFacility(stop1);
//		schedule.addStopFacility(stop2);
//		schedule.addStopFacility(stop3);
//		schedule.addStopFacility(stop4);
//		schedule.addStopFacility(stop5);
//		schedule.addStopFacility(stop6);
//		schedule.addStopFacility(stop7);
//		schedule.addStopFacility(stop8);
//
//		TransitLine tLine1 = builder.createTransitLine(this.ids[1]);
//		NetworkRouteWRefs networkRoute = (NetworkRouteWRefs) this.scenario.getNetwork().getFactory().createRoute(TransportMode.car, link1, link13);
//		ArrayList<Link> linkList = new ArrayList<Link>(6);
//		linkList.add(link3);
//		linkList.add(link5);
//		linkList.add(link7);
//		linkList.add(link8);
//		linkList.add(link9);
//		linkList.add(link11);
//		networkRoute.setLinks(link1, linkList, link13);
//		ArrayList<TransitRouteStop> stopList = new ArrayList<TransitRouteStop>(4);
//		stopList.add(builder.createTransitRouteStop(stop1, 0, 0));
//		stopList.add(builder.createTransitRouteStop(stop3, 90, 100));
//		stopList.add(builder.createTransitRouteStop(stop4, 290, 300));
//		stopList.add(builder.createTransitRouteStop(stop5, 390, Time.UNDEFINED_TIME));
//		TransitRoute tRoute1 = builder.createTransitRoute(this.ids[1], networkRoute, stopList, TransportMode.bus);
//		tLine1.addRoute(tRoute1);
//
//		tRoute1.addDeparture(builder.createDeparture(this.ids[1], Time.parseTime("07:00:00")));
//		tRoute1.addDeparture(builder.createDeparture(this.ids[2], Time.parseTime("07:05:00")));
//		tRoute1.addDeparture(builder.createDeparture(this.ids[3], Time.parseTime("07:10:00")));
//		tRoute1.addDeparture(builder.createDeparture(this.ids[4], Time.parseTime("07:15:00")));
//		tRoute1.addDeparture(builder.createDeparture(this.ids[5], Time.parseTime("07:20:00")));
//		tRoute1.addDeparture(builder.createDeparture(this.ids[6], Time.parseTime("07:25:00")));
//		
//		schedule.addTransitLine(tLine1);
//
//		TransitLine tLine2 = builder.createTransitLine(this.ids[2]);
//		networkRoute = (NetworkRouteWRefs) this.scenario.getNetwork().getFactory().createRoute(TransportMode.car, link2, link12);
//		linkList = new ArrayList<Link>(6);
//		linkList.add(link4);
//		linkList.add(link6);
//		linkList.add(link7);
//		linkList.add(link8);
//		linkList.add(link9);
//		linkList.add(link10);
//		networkRoute.setLinks(link2, linkList, link12);
//		stopList = new ArrayList<TransitRouteStop>(4);
//		stopList.add(builder.createTransitRouteStop(stop2, 0, 0));
//		stopList.add(builder.createTransitRouteStop(stop3, 90, 100));
//		stopList.add(builder.createTransitRouteStop(stop4, 290, 300));
//		stopList.add(builder.createTransitRouteStop(stop6, 390, Time.UNDEFINED_TIME));
//		TransitRoute tRoute2 = builder.createTransitRoute(this.ids[1], networkRoute, stopList, TransportMode.bus);
//		tLine2.addRoute(tRoute2);
//		tRoute2.addDeparture(builder.createDeparture(this.ids[1], Time.parseTime("07:02:00")));
//		tRoute2.addDeparture(builder.createDeparture(this.ids[2], Time.parseTime("07:12:00")));
//		tRoute2.addDeparture(builder.createDeparture(this.ids[3], Time.parseTime("07:22:00")));
//
//		networkRoute = (NetworkRouteWRefs) this.scenario.getNetwork().getFactory().createRoute(TransportMode.car, link17, link19);
//		linkList = new ArrayList<Link>(6);
//		linkList.add(link18);
//		linkList.add(link14);
//		linkList.add(link15);
//		linkList.add(link16);
//		networkRoute.setLinks(link17, linkList, link19);
//		stopList = new ArrayList<TransitRouteStop>(2);
//		stopList.add(builder.createTransitRouteStop(stop7, 0, 0));
//		stopList.add(builder.createTransitRouteStop(stop8, 390, Time.UNDEFINED_TIME));
//		TransitRoute tRoute2a = builder.createTransitRoute(this.ids[2], networkRoute, stopList, TransportMode.bus);
//		tLine2.addRoute(tRoute2a);
//		tRoute2a.addDeparture(builder.createDeparture(this.ids[1], Time.parseTime("07:18:00")));
//		tRoute2a.addDeparture(builder.createDeparture(this.ids[2], Time.parseTime("07:28:00")));
//		tRoute2a.addDeparture(builder.createDeparture(this.ids[3], Time.parseTime("07:38:00")));
//		
//		schedule.addTransitLine(tLine2);
//	}
//
//	private void createPopulation() {
//		TransitSchedule schedule = this.scenario.getTransitSchedule();
//		PopulationImpl population = this.scenario.getPopulation();
//		PopulationFactory pb = population.getFactory();
//
//		TransitLine tLine1 = schedule.getTransitLines().get(this.ids[1]);
//		TransitRoute tRoute1 = tLine1.getRoutes().get(this.ids[1]);
//		TransitLine tLine2 = schedule.getTransitLines().get(this.ids[2]);
//		TransitRoute tRoute2 = tLine1.getRoutes().get(this.ids[1]);
//
//		TransitStopFacility stop1 = schedule.getFacilities().get(this.ids[1]);
//		/*TransitStopFacility stop2 =*/ schedule.getFacilities().get(this.ids[2]);
//		TransitStopFacility stop3 = schedule.getFacilities().get(this.ids[3]);
//		TransitStopFacility stop4 = schedule.getFacilities().get(this.ids[4]);
//		/*TransitStopFacility stop5 =*/ schedule.getFacilities().get(this.ids[5]);
//		TransitStopFacility stop6 = schedule.getFacilities().get(this.ids[6]);
//
//		{ // person 1
//			PersonImpl person = (PersonImpl) pb.createPerson(this.ids[1]);
//			PlanImpl plan = (PlanImpl) pb.createPlan();
//			ActivityImpl act1 = (ActivityImpl) pb.createActivityFromLinkId("home", this.ids[1]);
//			act1.setEndTime(Time.parseTime("07:01:00"));
//			LegImpl leg1 = (LegImpl) pb.createLeg(TransportMode.pt);
//			leg1.setRoute(new ExperimentalTransitRoute(stop1, tLine1, tRoute1, stop3));
//			ActivityImpl act2 = (ActivityImpl) pb.createActivityFromLinkId("pt interaction", this.ids[3]);
//			act2.setEndTime(Time.parseTime("07:01:00"));
//			LegImpl leg2 = (LegImpl) pb.createLeg(TransportMode.pt);
//			leg2.setRoute(new ExperimentalTransitRoute(stop3, tLine2, tRoute2, stop6));
//			ActivityImpl act3 = (ActivityImpl) pb.createActivityFromLinkId("pt interaction", this.ids[6]);
//
//			plan.addActivity(act1);
//			plan.addLeg(leg1);
//			plan.addActivity(act2);
//			plan.addLeg(leg2);
//			plan.addActivity(act3);
//			person.addPlan(plan);
//			person.setSelectedPlan(plan);
//			population.addPerson(person);
//		}
//
//		{ // person 2
//			PersonImpl person = (PersonImpl) pb.createPerson(this.ids[2]);
//			PlanImpl plan = (PlanImpl) pb.createPlan();
//			ActivityImpl act1 = (ActivityImpl) pb.createActivityFromLinkId("home", this.ids[1]);
//			act1.setEndTime(Time.parseTime("07:06:00"));
//			LegImpl leg1 = (LegImpl) pb.createLeg(TransportMode.pt);
//			leg1.setRoute(new ExperimentalTransitRoute(stop1, tLine1, tRoute1, stop3));
//			ActivityImpl act2 = (ActivityImpl) pb.createActivityFromLinkId("pt interaction", this.ids[3]);
//			act2.setEndTime(Time.parseTime("07:06:00"));
//			LegImpl leg2 = (LegImpl) pb.createLeg(TransportMode.pt);
//			leg2.setRoute(new ExperimentalTransitRoute(stop3, tLine2, tRoute2, stop6));
//			ActivityImpl act3 = (ActivityImpl) pb.createActivityFromLinkId("pt interaction", this.ids[6]);
//
//			plan.addActivity(act1);
//			plan.addLeg(leg1);
//			plan.addActivity(act2);
//			plan.addLeg(leg2);
//			plan.addActivity(act3);
//			person.addPlan(plan);
//			person.setSelectedPlan(plan);
//			population.addPerson(person);
//		}
//
//		{ // person 3
//			PersonImpl person = (PersonImpl) pb.createPerson(this.ids[3]);
//			PlanImpl plan = (PlanImpl) pb.createPlan();
//			ActivityImpl act1 = (ActivityImpl) pb.createActivityFromLinkId("home", this.ids[1]);
//			act1.setEndTime(Time.parseTime("07:11:00"));
//			LegImpl leg1 = (LegImpl) pb.createLeg(TransportMode.pt);
//			leg1.setRoute(new ExperimentalTransitRoute(stop1, tLine1, tRoute1, stop4));
//			ActivityImpl act2 = (ActivityImpl) pb.createActivityFromLinkId("pt interaction", this.ids[3]);
//			act2.setEndTime(Time.parseTime("07:11:00"));
//			LegImpl leg2 = (LegImpl) pb.createLeg(TransportMode.pt);
//			leg2.setRoute(new ExperimentalTransitRoute(stop4, tLine2, tRoute2, stop6));
//			ActivityImpl act3 = (ActivityImpl) pb.createActivityFromLinkId("pt interaction", this.ids[6]);
//
//			plan.addActivity(act1);
//			plan.addLeg(leg1);
//			plan.addActivity(act2);
//			plan.addLeg(leg2);
//			plan.addActivity(act3);
//			person.addPlan(plan);
//			person.setSelectedPlan(plan);
//			population.addPerson(person);
//		}
//	}
//	
//	private void buildUmlaeufe() {
//		Collection<TransitLine> transitLines = scenario.getTransitSchedule().getTransitLines().values();
//		GreedyUmlaufBuilderImpl greedyUmlaufBuilder = new GreedyUmlaufBuilderImpl(new UmlaufInterpolator(scenario.getNetwork()), transitLines);
//		Collection<Umlauf> umlaeufe = greedyUmlaufBuilder.build();
//		VehiclesFactory vb = this.scenario.getVehicles().getFactory();
//		BasicVehicleType vehicleType = vb.createVehicleType(new IdImpl(
//				"defaultTransitVehicleType"));
//		BasicVehicleCapacity capacity = new BasicVehicleCapacityImpl();
//		capacity.setSeats(Integer.valueOf(101));
//		capacity.setStandingRoom(Integer.valueOf(0));
//		vehicleType.setCapacity(capacity);
//		this.scenario.getVehicles().getVehicleTypes().put(vehicleType.getId(),
//				vehicleType);
//
//		for (Umlauf umlauf : umlaeufe) {
//			Id vehId = umlauf.getId();
//			BasicVehicle veh = vb.createVehicle(vehId, vehicleType);
//			this.scenario.getVehicles().getVehicles().put(veh.getId(), veh);
//			umlauf.setVehicleId(veh.getId());
//		}
//
//	}
//
//	private void runSim() {
//		EventsManagerImpl events = new EventsManagerImpl();
//		VehicleTracker vehTracker = new VehicleTracker();
//		events.addHandler(vehTracker);
//		TransitRoute route2 = this.scenario.getTransitSchedule().getTransitLines().get(this.ids[2]).getRoutes().get(this.ids[1]);
//		RouteOccupancy analysis2 = new RouteOccupancy(route2, vehTracker);
//		events.addHandler(analysis2);
//		TransitQueueSimulation sim = new TransitQueueSimulation(this.scenario, events);
//		sim.setUseUmlaeufe(true);
//		sim.startOTFServer("two_lines_demo");
//		OTFDemo.ptConnect("two_lines_demo", this.scenario.getConfig());
//
//		sim.run();
//
//		System.out.println("stop\t#exitleaving\t#enter\t#inVehicle");
//		int inVehicle = 0;
//
//		for (TransitRouteStop stop : route2.getStops()) {
//			Id stopId = stop.getStopFacility().getId();
//			int enter = analysis2.getNumberOfEnteringPassengers(stopId);
//			int leave = analysis2.getNumberOfLeavingPassengers(stopId);
//			inVehicle = inVehicle + enter - leave;
//			System.out.println(stopId + "\t" + leave + "\t" + enter + "\t" + inVehicle);
//		}
//	}
//
//	public void run() {
//		createIds();
//		prepareConfig();
//		createNetwork();
//		createTransitSchedule();
//		createPopulation();
//		buildUmlaeufe();
//		runSim();
//	}
//
//	public static void main(final String[] args) {
//		new TwoLinesDemo().run();
//	}
//
//}