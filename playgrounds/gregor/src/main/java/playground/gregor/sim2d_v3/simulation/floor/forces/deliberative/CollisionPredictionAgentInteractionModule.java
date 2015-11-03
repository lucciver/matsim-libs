/* *********************************************************************** *
 * project: org.matsim.*
 * CollisionPredictionAgentInteractionModule.java
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

package playground.gregor.sim2d_v3.simulation.floor.forces.deliberative;

import java.util.Collection;

import org.matsim.api.core.v01.Scenario;

import playground.gregor.sim2d_v3.config.Sim2DConfigGroup;
import playground.gregor.sim2d_v3.simulation.floor.Agent2D;
import playground.gregor.sim2d_v3.simulation.floor.PhysicalAgentRepresentation;
import playground.gregor.sim2d_v3.simulation.floor.PhysicalFloor;
import playground.gregor.sim2d_v3.simulation.floor.forces.DynamicForceModule;
import playground.gregor.sim2d_v3.simulation.floor.forces.deliberative.velocityobstacle.Algorithms;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class CollisionPredictionAgentInteractionModule implements DynamicForceModule {

	private final PhysicalFloor floor;

	private final double quadUpdateInterval = 0.1;
	private double lastQuadUpdate = Double.NEGATIVE_INFINITY;

	private final double EventHorizonTime = 10;
	private final GeometryFactory geofac = new GeometryFactory();

	//Zanlungo et al constant
	public double Bi=.71;
	public double Ai=1.13*PhysicalAgentRepresentation.AGENT_WEIGHT;
	public double lambda=.29;

	protected  Quadtree agentsQuad;

	//Laemmel constant
	private static final double neighborhoodSensingRange = 5;

	public CollisionPredictionAgentInteractionModule(PhysicalFloor floor, Scenario scenario) {
		this.floor = floor;
		Sim2DConfigGroup conf = (Sim2DConfigGroup) scenario.getConfig().getModule("sim2d");
		this.Bi = conf.getBi();
		this.Ai = conf.getAi();
		this.lambda = conf.getLambda();
	}

	@Override
	public void run(Agent2D agent,double time) {
		double fx = 0;
		double fy = 0;

		double minX = agent.getPosition().x - neighborhoodSensingRange;
		double maxX = agent.getPosition().x + neighborhoodSensingRange;
		double minY = agent.getPosition().y - neighborhoodSensingRange;
		double maxY = agent.getPosition().y + neighborhoodSensingRange;
		Envelope e = new Envelope(minX, maxX, minY, maxY);
		@SuppressWarnings("unchecked")
		Collection<Agent2D> l = this.agentsQuad.query(e);
		double t_i = getTi(l,agent);


		if (t_i == Double.POSITIVE_INFINITY || t_i <= 0.0001) {
			return;
		}
		double v_i = Math.sqrt(agent.getVx()*agent.getVx() + agent.getVy()*agent.getVy());
		double decel = v_i / t_i;

		for (Agent2D other : l) {
			if (other == agent) {
				continue;
			}

			double dist = other.getPosition().distance(agent.getPosition());
			if (dist > neighborhoodSensingRange || dist < 0.1) {
				continue;
			}
			double term1 = this.Ai * decel * Math.exp(-dist/this.Bi);

			Vector v = getDistVector(agent,other,t_i);
			double projectedDist = Math.max(0.01, Math.sqrt(v.x*v.x+v.y*v.y));


			double phi = getPhi(agent,other);
			double anostropyWeight = (this.lambda + (1-this.lambda)*(1+Math.cos(phi))/2);


			fx += anostropyWeight * term1 * v.x/projectedDist;
			fy += anostropyWeight * term1 * v.y/projectedDist;
		}

		agent.getForce().incrementX(fx);
		agent.getForce().incrementY(fy);
	}

	private double getPhi(Agent2D agent, Agent2D other) {
		double phi = 0;
		double dist = agent.getPosition().distance(other.getPosition()) ;
		LineString ls = this.geofac.createLineString(new Coordinate[]{agent.getPosition(),new Coordinate(agent.getPosition().x+agent.getVx()*1000,agent.getPosition().y+agent.getVy()*1000)});
		DistanceOp op =  new DistanceOp(ls, this.geofac.createPoint(other.getPosition()));
		if (op.distance() >= dist) {
			ls = this.geofac.createLineString(new Coordinate[]{agent.getPosition(),new Coordinate(agent.getPosition().x-agent.getVx()*1000,agent.getPosition().y-agent.getVy()*1000)});
			op =  new DistanceOp(ls, this.geofac.createPoint(other.getPosition()));
			double sinPhi = Math.min(1, op.distance()/dist);
//			phi = Math.PI - Math.asin(sinPhi);
			phi = Math.PI - Algorithms.lookupAsin(sinPhi);
		} else {
			double sinPhi = op.distance()/dist;
//			phi = Math.asin(sinPhi);
			phi = Algorithms.lookupAsin(sinPhi);
		}

		return phi;
	}

	private Vector getDistVector(Agent2D agent, Agent2D other, double t_i) {
		double projectedOtherX = other.getPosition().x + other.getVx() * t_i;
		double projectedOtherY = other.getPosition().y + other.getVy() * t_i;

		double projectedX = agent.getPosition().x + agent.getVx() * t_i;
		double projectedY = agent.getPosition().y + agent.getVy() * t_i;

		double dPrimeX_ij = projectedX - projectedOtherX;
		double dPrimeY_ij = projectedY - projectedOtherY;

		Vector v = new Vector();
		v.x = dPrimeX_ij;
		v.y = dPrimeY_ij;

		return v;
	}

	private double getTi(Collection<Agent2D> l, Agent2D agent) {

		double t_i = Double.POSITIVE_INFINITY;

		for (Agent2D other : l) {
			if (other == agent) {
				continue;
			}
			double tmp = getTi(other,agent);
			if (tmp < t_i) {
				t_i = tmp;
			}
		}
		return t_i;
	}

	private double getTi(Agent2D other, Agent2D agent) {
		double relVx= other.getVx() - agent.getVx();
		double relVy = other.getVy() - agent.getVy();

		double relV = Math.sqrt(relVx*relVx + relVy * relVy);

		LineString ls = this.geofac.createLineString(new Coordinate[] {other.getPosition(),new Coordinate(other.getPosition().x+relVx*this.EventHorizonTime,other.getPosition().y+relVy*this.EventHorizonTime,0)});
		DistanceOp op =  new DistanceOp(ls, this.geofac.createPoint(agent.getPosition()));
		double ti = op.closestPoints()[0].distance(other.getPosition());


		double tanTheta = op.distance()/ti;
//		double theta = Math.atan(tanTheta);
		double theta = Algorithms.lookupAtan(tanTheta);

		if (theta > Math.PI/4){
			return Double.POSITIVE_INFINITY;
		}
		ti /= relV;
		return ti;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public void update(double time) {
		if (time >= this.lastQuadUpdate + this.quadUpdateInterval) {

			updateAgentQuadtree();

			this.lastQuadUpdate = time;
		}
	}

	@Override
	public void forceUpdate() {
		// TODO Auto-generated method stub
	}

	protected void updateAgentQuadtree() {
		this.agentsQuad = new Quadtree();
		for (Agent2D agent : this.floor.getAgents()) {
			Envelope e = new Envelope(agent.getPosition());
			this.agentsQuad.insert(e, agent);
		}

	}

	private static final class Vector {
		double x;
		double y;
	}
}