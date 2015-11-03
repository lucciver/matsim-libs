/* *********************************************************************** *
 * project: org.matsim.*
 * LinkEnterCountHandler.java
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
package playground.benjamin.spacetimegeo;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.AgentMoneyEvent;
import org.matsim.core.api.experimental.events.LinkLeaveEvent;
import org.matsim.core.api.experimental.events.handler.AgentMoneyEventHandler;
import org.matsim.core.api.experimental.events.handler.LinkLeaveEventHandler;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.controler.Controler;

/**
 * @author benjamin
 *
 */
public class LinkLeaveCountHandler implements LinkLeaveEventHandler, AgentMoneyEventHandler {
	private static Logger logger = Logger.getLogger(LinkLeaveCountHandler.class);
	
	Controler controler;

	double link3Counter = 0.0;
	double link9Counter = 0.0;
	double link11Counter = 0.0;
	
	double tollPaid = 0.0;


	public LinkLeaveCountHandler(Controler controler) {
		this.controler = controler;
	}

	@Override
	public void reset(int iteration) {
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		Id linkId = event.getLinkId();
		
		if(linkId.equals(new IdImpl("3"))){
			logger.info("The agent is chosing route 3 in iteration " + this.controler.getIterationNumber());
			link3Counter++;
		} else if(linkId.equals(new IdImpl("9"))){
			logger.info("The agent is chosing route 9 in iteration " + this.controler.getIterationNumber());
			link9Counter++;
		} else if(linkId.equals(new IdImpl("11"))){
			logger.info("The agent is chosing route 11 in iteration " + this.controler.getIterationNumber());
			link11Counter++;
		} else {
			// do nothing
		}
	}

	protected double getLink3Counter() {
		return link3Counter;
	}

	protected double getLink9Counter() {
		return link9Counter;
	}

	protected double getLink11Counter() {
		return link11Counter;
	}

	@Override
	public void handleEvent(AgentMoneyEvent event) {
		tollPaid += event.getAmount();
		logger.info("The agent is paying " + event.getAmount());
	}

	protected double getTollPaid() {
		return tollPaid;
	}
}