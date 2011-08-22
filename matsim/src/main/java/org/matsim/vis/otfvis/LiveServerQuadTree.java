/* *********************************************************************** *
 * project: org.matsim.*
 * OTFQSimServerQuad
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
package org.matsim.vis.otfvis;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.vis.otfvis.data.OTFConnectionManager;
import org.matsim.vis.otfvis.data.OTFDataWriter;
import org.matsim.vis.otfvis.data.OTFServerQuadTree;
import org.matsim.vis.otfvis.data.OTFWriterFactory;
import org.matsim.vis.snapshotwriters.VisLink;
import org.matsim.vis.snapshotwriters.VisNetwork;
import org.matsim.vis.snapshotwriters.VisNode;

/**
 * @author dgrether
 * 
 */
class LiveServerQuadTree extends OTFServerQuadTree {

	private static final long serialVersionUID = 23L;

	private static final Logger log = Logger.getLogger(LiveServerQuadTree.class);

	transient private VisNetwork net;

	public LiveServerQuadTree(VisNetwork net) {
		super(net.getNetwork());
		this.net = net;
	}

	@Override
	public void initQuadTree(OTFConnectionManager connect) {
		createFactoriesAndFillQuadTree(connect);
	}

	private void createFactoriesAndFillQuadTree(OTFConnectionManager connect) {
		Collection<Class<OTFWriterFactory<VisNode>>> nodeFactories = connect.getNodeWriters();
		List<OTFWriterFactory<VisNode>> nodeWriterFractoryObjects = instanciateFactories(nodeFactories);
		installNodeWriterFactories(nodeWriterFractoryObjects);

		Collection<Class<OTFWriterFactory<VisLink>>> linkFactories = connect.getLinkWriters();
		List<OTFWriterFactory<VisLink>> linkWriterFactoryObjects = instanciateFactories(linkFactories);
		installLinkWriterFactories(linkWriterFactoryObjects);
	}

	private static <T> List<OTFWriterFactory<T>> instanciateFactories(Collection<Class<OTFWriterFactory<T>>> nodeFactories) {
		List<OTFWriterFactory<T>> writerFactoryObjects = new ArrayList<OTFWriterFactory<T>>();
		OTFWriterFactory<T> writerFactory = null;
		for (Class<? extends OTFWriterFactory<T>> writerFactoryClass : nodeFactories) {
			try {
				writerFactory = writerFactoryClass.newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			writerFactoryObjects.add(writerFactory);
		}
		return writerFactoryObjects;
	}

	private void installLinkWriterFactories(
			List<OTFWriterFactory<VisLink>> linkWriterFactoryObjects) {
		for (VisLink link : this.net.getVisLinks().values()) {
			Point2D.Double fromCoord = transform(link.getLink().getFromNode().getCoord());
			Point2D.Double toCoord = transform(link.getLink().getToNode().getCoord());
			double middleEast = (toCoord.getX() + fromCoord.getX()) * 0.5;
			double middleNorth = (toCoord.getY() + fromCoord.getY()) * 0.5;
			for (OTFWriterFactory<VisLink> fac : linkWriterFactoryObjects) {
				OTFDataWriter<VisLink> writer = fac.getWriter();
				// null means take the default handler
				if (writer != null) {
					writer.setSrc(link);
				}
				this.put(middleEast, middleNorth, writer);
			}
		}
	}

	private void installNodeWriterFactories(
			List<OTFWriterFactory<VisNode>> nodeWriterFractoryObjects) {
		boolean first = true;
		for (VisNode node : this.net.getVisNodes().values()) {
			for (OTFWriterFactory<VisNode> fac : nodeWriterFractoryObjects) {
				OTFDataWriter<VisNode> writer = fac.getWriter();
				if (writer != null) {
					writer.setSrc(node);
					if (first) {
						log.info("Connecting Source QNode with "
								+ writer.getClass().getName());
						first = false;
					}
				}
				Point2D.Double coord = transform(node.getNode().getCoord());
				this.put(coord.getX(), coord.getY(), writer);
			}
		}
	}

}
