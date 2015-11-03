/* *********************************************************************** *
 * project: org.matsim.*
 * Section.java
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

package playground.gregor.sim2d_v4.scenario;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;

import com.vividsolutions.jts.geom.Polygon;

public class Section implements Identifiable {

	private final Id id;
	private final Polygon p;
	private int[] openings = null;
	private Id[] neighbors = null;
	private final int level;
	private final List<Id> relatedLinks = new ArrayList<Id>();

	/*package*/ Section(Id id, Polygon p, int[] openings, Id[] neighbors, int level) {
		this.id = id;
		this.p = p; //maybe we can sparse the polygon here and take the coordinate array of its exterior ring instead [gl Jan' 2013]
		this.openings = openings;
		this.neighbors = neighbors;
		this.level = level;
	}

	public Polygon getPolygon() {
		return this.p;
	}

	public int getLevel() {
		return this.level;
	}

	public int[] getOpenings() {
		return this.openings;
	}
	
	public Id[] getNeighbors() {
		return this.neighbors;
	}
	
	@Override
	public Id getId() {
		return this.id;
	}

	public void addRelatedLinkId(Id id2) {
		this.relatedLinks.add(id2);
	}
	
	public List<Id> getRelatedLinkIds() {
		return this.relatedLinks;
	}
	
	
}