/* *********************************************************************** *
 * project: org.matsim.*
 * ExtractHitchHikersFakeActivities.java
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
package playground.thibautd.hitchiking.analysis;

import org.matsim.core.api.experimental.events.ActivityEndEvent;
import org.matsim.core.api.experimental.events.ActivityStartEvent;
import org.matsim.core.api.experimental.events.AgentArrivalEvent;
import org.matsim.core.api.experimental.events.Event;
import org.matsim.core.api.experimental.events.EventsFactory;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.handler.AgentArrivalEventHandler;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.events.handler.BasicEventHandler;

import playground.thibautd.hitchiking.HitchHikingConstants;

/**
 * Parses an event file and creates fake activity starts and ends for the
 * following events:
 * <ul>
 * <li> "waitingTime" activity for the duration for which an agent waits for a driver
 * <li> departure and arrival activities ant pu and do
 * </ul>
 * @author thibautd
 */
public class ExtractHitchHikersFakeActivities {
	public static final String WAIT_ACT_TYPE = "waitingTime";
	public static final String DEP_ACT_TYPE = "passengerDeparture";
	public static final String ARR_ACT_TYPE = "passengerArrival";
	private static final double DUR = 60;

	public static void main(final String[] args) {
		String eventsFile = args[0];
		String outFile = args[1];

		Handler handler = new Handler( outFile );
		EventsManager events = EventsUtils.createEventsManager();
		events.addHandler( handler );
		(new MatsimEventsReader( events )).readFile( eventsFile );
		handler.writer.closeFile();
	}

	private static class Handler implements BasicEventHandler, AgentArrivalEventHandler {
		private final EventWriterXML writer;
		private final EventsFactory factory = new EventsFactory();

		public Handler(final String outFile) {
			writer = new EventWriterXML( outFile );
			writer.init( outFile );
		}

		@Override
		public void reset(final int iteration) {
		}

		@Override
		public void handleEvent(final AgentArrivalEvent event) {
			if (event.getLegMode().equals( HitchHikingConstants.PASSENGER_MODE )) {
				writer.handleEvent(
						factory.createActivityStartEvent(
							event.getTime(),
							event.getPersonId(),
							event.getLinkId(),
							null,
							ARR_ACT_TYPE));
				writer.handleEvent(
						factory.createActivityEndEvent(
							event.getTime() + DUR,
							event.getPersonId(),
							event.getLinkId(),
							null,
							ARR_ACT_TYPE));
			}
		}

		@Override
		public void handleEvent(final Event event) {
			if (event.getAttributes().get( Event.ATTRIBUTE_TYPE ).equals( "passengerStartsWaiting" )) {
				writer.handleEvent(
						factory.createActivityStartEvent(
							event.getTime(),
							new IdImpl( event.getAttributes().get( ActivityStartEvent.ATTRIBUTE_PERSON ) ),
							new IdImpl( event.getAttributes().get( "link" ) ),
							null,
							WAIT_ACT_TYPE));
			}
			else if (event.getAttributes().get( Event.ATTRIBUTE_TYPE ).equals( "passengerEndsWaiting" )) {
				writer.handleEvent(
						factory.createActivityEndEvent(
							event.getTime(),
							new IdImpl( event.getAttributes().get( ActivityEndEvent.ATTRIBUTE_PERSON ) ),
							new IdImpl( event.getAttributes().get( "link" ) ),
							null,
							WAIT_ACT_TYPE));

				writer.handleEvent(
						factory.createActivityStartEvent(
							event.getTime(),
							new IdImpl( event.getAttributes().get( ActivityStartEvent.ATTRIBUTE_PERSON ) ),
							new IdImpl( event.getAttributes().get( "link" ) ),
							null,
							DEP_ACT_TYPE));
				writer.handleEvent(
						factory.createActivityEndEvent(
							event.getTime() + DUR,
							new IdImpl( event.getAttributes().get( ActivityEndEvent.ATTRIBUTE_PERSON ) ),
							new IdImpl( event.getAttributes().get( "link" ) ),
							null,
							DEP_ACT_TYPE));
			}		
		}
	}
}
