/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.contrib.dvrp.passenger;

import static org.matsim.core.config.groups.PlanCalcScoreConfigGroup.createStageActivityType;
import static org.matsim.core.router.TripStructureUtils.Trip;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.PlanAgent;
import org.matsim.core.mobsim.qsim.ActivityEngine;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.agents.PlanBasedDriverAgentImpl;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.mobsim.qsim.interfaces.ActivityHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.mobsim.qsim.interfaces.TripInfoRequest;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.facilities.Facility;

public class ActivityEngineWithWakeup implements MobsimEngine, ActivityHandler {
	private static final Logger log = Logger.getLogger( ActivityEngine.class );

	private final ActivityFacilities facilities;
	private final BookingEngine bookingEngine;

	private ActivityEngine delegate;

	static StageActivityTypes drtStageActivities = new StageActivityTypesImpl(
			createStageActivityType(TransportMode.drt), createStageActivityType(TransportMode.walk));

	private final Queue<AgentAndLegEntry> wakeUpList = new PriorityBlockingQueue<>(500, (o1, o2) -> {
		int cmp = Double.compare(o1.time, o2.time);
		return cmp != 0 ? cmp : o1.agent.getId().compareTo(o2.agent.getId());
	});

	@Inject
	ActivityEngineWithWakeup(EventsManager eventsManager, Scenario scenario, BookingEngine bookingEngine) {
		this.delegate = new ActivityEngine(eventsManager);
		this.facilities = scenario.getActivityFacilities();
		this.bookingEngine = bookingEngine;
	}

	@Override
	public void onPrepareSim() {
		delegate.onPrepareSim();
	}

	@Override
	public void doSimStep(double time) {
		while (!wakeUpList.isEmpty()) {
			if (wakeUpList.peek().time <= time) {
				final AgentAndLegEntry entry = wakeUpList.poll();
				entry.executeOnWakeUp.accept(entry);
			}
		}
		delegate.doSimStep(time);
	}

	@Override
	public void afterSim() {
		delegate.afterSim();
	}

	@Override
	public void setInternalInterface( InternalInterface internalInterface ) {
		delegate.setInternalInterface(internalInterface);
	}

	/**
	 * This method is called by QSim to pass in agents which then "live" in the activity layer until they are handed out again
	 * through the internalInterface.
	 * <p>
	 * It is called not before onPrepareSim() and not after afterSim(), but it may be called before, after, or from doSimStep(),
	 * and even from itself (i.e. it must be reentrant), since internalInterface.arrangeNextAgentState() may trigger
	 * the next Activity.
	 */
	@Override
	public boolean handleActivity(MobsimAgent agent) {

		if ( agent instanceof PlanAgent ){
			// (we don't want to treat DvrpAgents, CarrierAgents, TransitVehicleDrivers etc. here)

			for( Leg drtLeg : findLegsWithModeInFuture( agent, TransportMode.drt ) ){
				Double prebookingOffset_s = (Double) drtLeg.getAttributes().getAttribute( "prebookingOffset_s" );
				if( prebookingOffset_s == null ){
					log.warn( "not prebooking" );
					continue;
				}
				final double prebookingTime = drtLeg.getDepartureTime() - prebookingOffset_s;
				if( prebookingTime < agent.getActivityEndTime() ){
					// yyyy and here one sees that having this in the activity engine is not very practical
					log.info( "adding agent to wakeup list" );
					this.wakeUpList.add( new AgentAndLegEntry( agent, prebookingTime, drtLeg, this::wakeUpAgent ) );
				}
			}
			for( Leg drtLeg : findLegsWithModeInFuture( agent, TransportMode.taxi ) ){
				Double prebookingOffset_s = (Double) drtLeg.getAttributes().getAttribute( "prebookingOffset_s" );
				if( prebookingOffset_s == null ){
					log.warn( "not prebooking" );
					continue;
				}
				final double prebookingTime = drtLeg.getDepartureTime() - prebookingOffset_s;
				if( prebookingTime < agent.getActivityEndTime() ){
					// yyyy and here one sees that having this in the activity engine is not very practical
					log.info( "adding agent to wakeup list" );
					this.wakeUpList.add( new AgentAndLegEntry( agent, prebookingTime, drtLeg, this::wakeUpAgent ) );
				}
			}
		}
		return delegate.handleActivity(agent);
	}

	private static List<Leg> findLegsWithModeInFuture( MobsimAgent agent, String mode ) {
		List<Leg> retVal = new ArrayList<>();
		Plan plan = WithinDayAgentUtils.getModifiablePlan(agent);
		for (int ii = WithinDayAgentUtils.getCurrentPlanElementIndex(agent); ii < plan.getPlanElements().size(); ii++) {
			PlanElement pe = plan.getPlanElements().get(ii);
			if (pe instanceof Leg) {
				if (Objects.equals(mode, ((Leg)pe).getMode())) {
					retVal.add((Leg)pe);
				}
			}
		}
		return retVal;
	}

	private void wakeUpAgent(AgentAndLegEntry entry) {
		Plan plan = WithinDayAgentUtils.getModifiablePlan(entry.agent);

		// search for drt trip corresponding to drt leg.  Trick is using our own stage activities.
		Trip drtTrip = TripStructureUtils.findTripAtPlanElement(entry.leg, plan, this.drtStageActivities);
		Gbl.assertNotNull(drtTrip);

		Facility fromFacility = FacilitiesUtils.toFacility(drtTrip.getOriginActivity(), facilities);
		Facility toFacility = FacilitiesUtils.toFacility(drtTrip.getDestinationActivity(), facilities);

		final TripInfoRequest request = new TripInfoRequest.Builder().setFromFacility(fromFacility)
				.setToFacility(toFacility)
				.setTime(drtTrip.getOriginActivity().getEndTime())
				.createRequest();

		//first simulate ActivityEngineWithWakeup and then BookingEngine --> decision process
		//in the same time step
		bookingEngine.notifyTripInfoRequestSent(entry.agent, request);
	}

	/**
	 * For within-day replanning. Tells this engine that the activityEndTime the agent reports may have changed since
	 * the agent was added to this engine through handleActivity.
	 * May be merged with handleActivity, since this engine can know by itself if it was called the first time
	 * or not.
	 *
	 * @param agent The agent.
	 */
	@Override
	public void rescheduleActivityEnd(final MobsimAgent agent) {
		delegate.rescheduleActivityEnd(agent);
	}

	/**
	 * Agents cannot be added directly to the activityEndsList since that would
	 * not be thread-safe when within-day replanning is used. There, an agent's
	 * activity end time can be modified. As a result, the agent is located at
	 * the wrong position in the activityEndsList until it is updated by using
	 * rescheduleActivityEnd(...). However, if another agent is added to the list
	 * in the mean time, it might be inserted at the wrong position.
	 * cdobler, apr'12
	 */
	static class AgentAndLegEntry {
		public AgentAndLegEntry(MobsimAgent agent, double time, Leg leg, Consumer<AgentAndLegEntry> executeOnWakeUp) {
			// yyyy be careful that the executeOnWakeUp does not become overkill here; if we want something more general, rather
			// move on a completely general MessageQueue.  kai, mar'19

			this.agent = agent;
			this.time = time;
			this.leg = leg;
			this.executeOnWakeUp = executeOnWakeUp;
		}

		final MobsimAgent agent;
		final double time;
		final Leg leg;
		final Consumer<AgentAndLegEntry> executeOnWakeUp;
	}

}
