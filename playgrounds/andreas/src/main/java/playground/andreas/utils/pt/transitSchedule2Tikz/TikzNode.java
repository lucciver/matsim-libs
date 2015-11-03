/* *********************************************************************** *
 * project: org.matsim.*
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
package playground.andreas.utils.pt.transitSchedule2Tikz;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

/**
 * @author droeder
 *
 */
public class TikzNode {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(TikzNode.class);
	private Coord coord;
	private Id id;

	public TikzNode(TransitStopFacility f, int i) {
		this.coord = f.getCoord();
//		this.id = new IdImpl(f.getId().toString().replaceAll("_", ""));
		this.id = new IdImpl(i);
	}
	
	public Coord getCoord(){
		return this.coord;
	}
	
	public String getTikzString(String styleId){
		return ("\\node [" + styleId + "] (" + this.id.toString() + 
				") at (" + this.coord.getX() + "," + this.coord.getY() +") {};");	
	}

	/**
	 * @return
	 */
	public Id getId() {
		return this.id;
	}

	/**
	 * @param xOffset
	 * @param yOffset
	 * @param scale 
	 * @return 
	 */
	public void offset(Double xOffset, Double yOffset, Double scale) {
		this.coord = new CoordImpl((this.coord.getX() + xOffset)* scale, (this.coord.getY() + yOffset) * scale);
	}
}
