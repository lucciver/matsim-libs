/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

/**
 *
 */
package org.matsim.contrib.av.robotaxi.fares.drt;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.events.handler.PersonMoneyEventHandler;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEvent;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerDroppedOffEvent;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.ParallelEventsManager;

/**
 * @author jbischoff
 */
public class DrtFareHandlerTest {

	/**
	 * Test method for {@link DrtFareHandler}.
	 */
	@Test
	public void testDrtFareHandler() {

		Config config = ConfigUtils.createConfig();
		DrtFareConfigGroup tccg = new DrtFareConfigGroup();
		config.addModule(tccg);
		tccg.setBasefare(1);
		tccg.setMinFarePerTrip(1.5);
		tccg.setDailySubscriptionFee(1);
		tccg.setDistanceFare_m(1.0 / 1000.0);
		tccg.setTimeFare_h(15);
		tccg.setMode(TransportMode.drt);

		final MutableDouble fare = new MutableDouble(0);
		ParallelEventsManager events = new ParallelEventsManager(false);
		DrtFareHandler tfh = new DrtFareHandler(tccg, events);
		events.addHandler(tfh);
		events.addHandler(new PersonMoneyEventHandler() {
			@Override
			public void handleEvent(PersonMoneyEvent event) {
				fare.add(event.getAmount());
			}

			@Override
			public void reset(int iteration) {
			}
		});
		events.initProcessing();

		var personId = Id.createPersonId("p1");
		String mode = TransportMode.drt;
		{
			var requestId = Id.create(0, Request.class);
			events.processEvent(new DrtRequestSubmittedEvent(0.0, mode, requestId, personId, Id.createLinkId("12"),
					Id.createLinkId("23"), 240, 1000));
			events.processEvent(new PassengerDroppedOffEvent(300.0, mode, requestId, personId, null));
			events.flush();

			//fare: 1 (daily fee) + 1 (distance()+ 1 basefare + 1 (time)
			Assert.assertEquals(-4.0, fare.getValue(), 0);
		}
		{
			// test minFarePerTrip
			var requestId = Id.create(1, Request.class);
			events.processEvent(new DrtRequestSubmittedEvent(0.0, mode, requestId, personId, Id.createLinkId("45"),
					Id.createLinkId("56"), 24, 100));
			events.processEvent(new PassengerDroppedOffEvent(300.0, mode, requestId, personId, null));
			events.finishProcessing();

			/*
			 * fare new trip: 0 (daily fee already paid) + 0.1 (distance)+ 1 basefare + 0.1 (time) = 1.2 < minFarePerTrip = 1.5
			 * --> new total fare: 4 (previous trip) + 1.5 (minFarePerTrip for new trip) = 5.5
			 */
			Assert.assertEquals(-5.5, fare.getValue(), 0);
		}
	}

}
