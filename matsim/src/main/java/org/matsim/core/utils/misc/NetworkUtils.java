/* *********************************************************************** *
 * project: org.matsim.*
 * NetworkUtils.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.core.utils.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.api.experimental.IdFactory;
import org.matsim.core.basic.v01.IdImpl;

/**
 * Contains several helper methods for working with {@link Network networks}.
 *
 * @author mrieser
 */
public class NetworkUtils {

	/**
	 * @param nodes
	 * @return The bounding box of all the given nodes as <code>double[] = {minX, minY, maxX, maxY}</code>
	 */
	public static double[] getBoundingBox(final Collection<? extends Node> nodes) {
		double[] bBox = new double[4];
		bBox[0] = Double.POSITIVE_INFINITY;
		bBox[1] = Double.POSITIVE_INFINITY;
		bBox[2] = Double.NEGATIVE_INFINITY;
		bBox[3] = Double.NEGATIVE_INFINITY;

		for (Node n : nodes) {
			if (n.getCoord().getX() < bBox[0]) {
				bBox[0] = n.getCoord().getX();
			}
			if (n.getCoord().getX() > bBox[2]) {
				bBox[2] = n.getCoord().getX();
			}
			if (n.getCoord().getY() > bBox[3]) {
				bBox[3] = n.getCoord().getY();
			}
			if (n.getCoord().getY() < bBox[1]) {
				bBox[1] = n.getCoord().getY();
			}
		}
		return bBox;
	}

	/**
	 * @param network
	 * @return array containing the nodes, sorted ascending by id.
	 */
	public static Node[] getSortedNodes(final Network network) {
		Node[] nodes = network.getNodes().values().toArray(new Node[network.getNodes().size()]);
		Arrays.sort(nodes, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		return nodes;
	}
	
	/**
	 * @param network
	 * @param nodes list of node ids, separated by one or multiple whitespace (space, \t, \n)
	 * @return list containing the specified nodes.
	 * @throws IllegalArgumentException if a specified node is not found in the network
	 */
	public static List<Node> getNodes(final Network network, final String nodes) {
		if (nodes == null) {
			return new ArrayList<Node>(0);
		}
		String trimmed = nodes.trim();
		if (trimmed.length() == 0) {
			return new ArrayList<Node>(0);
		}
		String[] parts = trimmed.split("[ \t\n]+");
		final List<Node> nodesList = new ArrayList<Node>(parts.length);

		for (String id : parts) {
			Node node = network.getNodes().get(new IdImpl(id));
			if (node == null) {
				throw new IllegalArgumentException("no node with id " + id);
			}
			nodesList.add(node);
		}
		return nodesList;
	}

	/**
	 * @param network
	 * @return array containing the links, sorted ascending by id.
	 */
	public static Link[] getSortedLinks(final Network network) {
		Link[] links = network.getLinks().values().toArray(new Link[network.getLinks().size()]);
		Arrays.sort(links, new Comparator<Link>() {
			@Override
			public int compare(Link o1, Link o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		return links;
	}
	
	/**
	 * @param network
	 * @param links list of link ids, separated by one or multiple whitespace (space, \t, \n)
	 * @return list containing the specified links.
	 * @throws IllegalArgumentException if a specified node is not found in the network
	 */
	public static List<Link> getLinks(final Network network, final String links) {
		if (links == null) {
			return new ArrayList<Link>(0);
		}
		String trimmed = links.trim();
		if (trimmed.length() == 0) {
			return new ArrayList<Link>(0);
		}
		String[] parts = trimmed.split("[ \t\n]+");
		final List<Link> linksList = new ArrayList<Link>(parts.length);

		for (String id : parts) {
			Link link = network.getLinks().get(new IdImpl(id));
			if (link == null) {
				throw new IllegalArgumentException("no link with id " + id);
			}
			linksList.add(link);
		}
		return linksList;
	}

	/**
	 * Splits the given string at whitespace (one or more space, tab, newline) into single pieces, which are interpreted as ids.
	 *
	 * @param links
	 * @return
	 * 
	 * @deprecated use the version which passes an {@link IdFactory}, otherwise you might end up wasting tons of memory
	 */
	@Deprecated
	public static List<Id> getLinkIds(final String links) {
		if (links == null) {
			return new ArrayList<Id>(0);
		}
		String trimmed = links.trim();
		if (trimmed.length() == 0) {
			return new ArrayList<Id>(0);
		}
		String[] parts = trimmed.split("[ \t\n]+");
		final List<Id> linkIdsList = new ArrayList<Id>(parts.length);

		for (String id : parts) {
			linkIdsList.add(new IdImpl(id));
		}
		return linkIdsList;
	}

	/**
	 * Splits the given string at whitespace (one or more space, tab, newline) into single pieces, which are interpreted as ids.
	 *
	 * @param links
	 * @return
	 */
	public static List<Id> getLinkIds(final String links, final IdFactory idFactory) {
		if (links == null) {
			return new ArrayList<Id>(0);
		}
		String trimmed = links.trim();
		if (trimmed.length() == 0) {
			return new ArrayList<Id>(0);
		}
		String[] parts = trimmed.split("[ \t\n]+");
		final List<Id> linkIdsList = new ArrayList<Id>(parts.length);
		
		for (String id : parts) {
			linkIdsList.add(idFactory.createId(id));
		}
		return linkIdsList;
	}

	public static List<Link> getLinks(final Network network, final List<Id> linkIds) {
		List<Link> links = new ArrayList<Link>();
		for (Id linkId : linkIds) {
			Link link = network.getLinks().get(linkId);
			if (link == null) {
				throw new IllegalArgumentException("no link with id " + linkId);
			}
			links.add(link);
		}
		return links;
	}

	public static List<Id> getLinkIds(final List<Link> links) {
		List<Id> linkIds = new ArrayList<Id>();
		if (links != null) {
			for (Link link : links) {
				linkIds.add(link.getId());
			}
		}
		return linkIds;
	}

	/**
	 * @return formerly, the maximum of 1 and the mathematically rounded number of lanes attribute's value at time "time" of the link given as parameter
	 *	now, the number is truncated, but 0 is never returned.
	 *	sorry, math.round is way, way too slow.
	 */
	public static int getNumberOfLanesAsInt(final double time, final Link link) {
		int numberOfLanes = (int) link.getNumberOfLanes(time);
		if (numberOfLanes == 0) {
			return 1;
		} else {
			return numberOfLanes;
		}
	}

	public static Map<Id, Link> getIncidentLinks(final Node n) {
		Map<Id, Link> links = new TreeMap<Id, Link>(n.getInLinks());
		links.putAll(n.getOutLinks());
		return links;
	}

	public static boolean isMultimodal(final Network network) {
		String mode = null;
		boolean hasEmptyModes = false;
		for (Link link : network.getLinks().values()) {
			Set<String> modes = link.getAllowedModes();
			if (modes.size() > 1) {
				return true; // it must be multimodal with more than 1 mode
			} else if (modes.size() == 1) {
				String m2 = modes.iterator().next();
				if (mode == null) {
					if (hasEmptyModes) {
						return true;
					}
					mode = m2;
				} else {
					if (!m2.equals(mode)) {
						return true;
					}
				}
			} else if (modes.size() == 0) {
				if (mode != null) {
					return true;
				}
				hasEmptyModes = true;
			}
		}
		return false;
	}

	public static Link getConnectingLink(final Node fromNode, final Node toNode) {
		for (Link link : fromNode.getOutLinks().values()) {
			if (link.getToNode() == toNode) {
				return link;
			}
		}
		return null;
	}
}