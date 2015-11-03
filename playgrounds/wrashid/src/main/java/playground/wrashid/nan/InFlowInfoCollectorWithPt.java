/* *********************************************************************** *
 * project: org.matsim.*
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

package playground.wrashid.nan;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.AgentWait2LinkEvent;
import org.matsim.core.api.experimental.events.LinkEnterEvent;
import org.matsim.core.api.experimental.events.handler.AgentWait2LinkEventHandler;
import org.matsim.core.api.experimental.events.handler.LinkEnterEventHandler;

public class InFlowInfoCollectorWithPt implements LinkEnterEventHandler,
		AgentWait2LinkEventHandler {

	private int binSizeInSeconds; // set the length of interval

	public HashMap<Id, int[]> linkInFlow;
	private Map<Id, ? extends Link> filteredEquilNetLinks; //

	private boolean isOldEventFile;

	public InFlowInfoCollectorWithPt(Map<Id, ? extends Link> filteredEquilNetLinks,
			boolean isOldEventFile, int binSizeInSeconds) {
		this.filteredEquilNetLinks = filteredEquilNetLinks;
		this.isOldEventFile = isOldEventFile;
		this.binSizeInSeconds=binSizeInSeconds;
	}

	@Override
	public void reset(int iteration) {
		linkInFlow = new HashMap<Id, int[]>(); // reset the variables (private
												// ones)
	}

	@Override
	public void handleEvent(LinkEnterEvent event) { // call from
													// NetworkReadExample
		enterLink(event.getLinkId(), event.getTime());
	}

	private void enterLink(Id linkId, double time) {
		if (!filteredEquilNetLinks.containsKey(linkId)) {
			return; // if the link is not in the link set, then exit the method
		}

		if (!linkInFlow.containsKey(linkId)) {
			linkInFlow.put(linkId, new int[(86400 / binSizeInSeconds) + 1]); // set
																				// the
																				// number
																				// of
																				// intervals
		}

		int[] bins = linkInFlow.get(linkId);

		int binIndex = (int) Math.round(Math.floor(time / binSizeInSeconds));

		if (time < 86400) {
			bins[binIndex] = bins[binIndex] + 1; // count the number of agents
													// each link each time
													// interval
		}
	}

	public void printLinkInFlow() { // print
		for (Id linkId : linkInFlow.keySet()) {
			int[] bins = linkInFlow.get(linkId);

			Link link = filteredEquilNetLinks.get(linkId);

			System.out.print(linkId + " - " + link.getCoord() + ": ");

			for (int i = 0; i < bins.length; i++) {
				System.out.print(bins[i] * 3600 / binSizeInSeconds + "\t");
			}

			System.out.println();
		}
	}

	public HashMap<Id, int[]> getLinkInFlow() {
		return linkInFlow;
	}

	@Override
	public void handleEvent(AgentWait2LinkEvent event) {
		enterLink(event.getLinkId(), event.getTime());		
	}

}