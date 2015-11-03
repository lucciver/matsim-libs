/* *********************************************************************** *
 * project: kai
 * EventsReduce.java
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

package playground.kai.analysis;

import org.matsim.core.api.experimental.events.AgentArrivalEvent;
import org.matsim.core.api.experimental.events.AgentDepartureEvent;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.handler.AgentArrivalEventHandler;
import org.matsim.core.api.experimental.events.handler.AgentDepartureEventHandler;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.algorithms.EventWriterXML;


class EventsReduce {
	
	class MyEventHandler1 implements AgentDepartureEventHandler, AgentArrivalEventHandler {
		
		final EventsManager evOut ;
		MyEventHandler1( EventsManager evOut ) {
			this.evOut = evOut ;
		}

		@Override
		public void handleEvent(AgentDepartureEvent event) {
			this.evOut.processEvent(event) ;
		}

		@Override
		public void handleEvent(AgentArrivalEvent event) {
			this.evOut.processEvent(event) ;
		}

		@Override
		public void reset(int iteration) {
			throw new UnsupportedOperationException();
		}

	}
	
	private void run() {
		String inputFile = "/Users/nagel/kairuns/16ba-ext-30jun/ITERS/it.100/100.events.xml.gz";

		EventsManager eventsOut = EventsUtils.createEventsManager() ;
		EventWriterXML writerHandler = new EventWriterXML("/Users/nagel/kw/reduced.events.xml.gz") ;
		eventsOut.addHandler(writerHandler) ;
		
		EventsManager eventsIn = EventsUtils.createEventsManager();

		MyEventHandler1 handler = new MyEventHandler1(eventsOut);
		eventsIn.addHandler(handler);
		

		MatsimEventsReader reader = new MatsimEventsReader(eventsIn);
		reader.readFile(inputFile);
		
		System.out.println("done");
		
		writerHandler.closeFile() ;

	}

	public static void main(String[] args) {
		new EventsReduce().run() ;
	}

}