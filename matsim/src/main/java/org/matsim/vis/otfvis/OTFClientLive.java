/* *********************************************************************** *
 * project: org.matsim.*
 * OnTheFlyClientQuad.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

import java.awt.BorderLayout;

import javax.swing.SwingUtilities;

import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.matsim.core.config.Config;
import org.matsim.lanes.otfvis.drawer.OTFLaneSignalDrawer;
import org.matsim.lanes.otfvis.io.OTFLaneReader;
import org.matsim.lanes.otfvis.io.OTFLaneWriter;
import org.matsim.pt.otfvis.FacilityDrawer;
import org.matsim.signalsystems.otfvis.io.OTFSignalReader;
import org.matsim.signalsystems.otfvis.io.OTFSignalWriter;
import org.matsim.vis.otfvis.caching.SimpleSceneLayer;
import org.matsim.vis.otfvis.data.OTFClientQuadTree;
import org.matsim.vis.otfvis.data.OTFConnectionManager;
import org.matsim.vis.otfvis.data.OTFServerQuadTree;
import org.matsim.vis.otfvis.data.fileio.SettingsSaver;
import org.matsim.vis.otfvis.gui.OTFHostControlBar;
import org.matsim.vis.otfvis.gui.OTFQueryControl;
import org.matsim.vis.otfvis.gui.OTFQueryControlToolBar;
import org.matsim.vis.otfvis.gui.OTFVisConfigGroup;
import org.matsim.vis.otfvis.handler.OTFAgentsListHandler;
import org.matsim.vis.otfvis.handler.OTFLinkAgentsHandler;
import org.matsim.vis.otfvis.interfaces.OTFServer;
import org.matsim.vis.otfvis.opengl.drawer.OTFOGLDrawer;
import org.matsim.vis.otfvis.opengl.layer.AgentPointDrawer;
import org.matsim.vis.otfvis.opengl.layer.OGLAgentPointLayer;
import org.matsim.vis.otfvis.opengl.layer.OGLSimpleQuadDrawer;
import org.matsim.vis.otfvis.opengl.layer.OGLSimpleStaticNetLayer;

public class OTFClientLive {

	public static void run(final Config config, final OTFServer server) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				OTFConnectionManager connectionManager = new OTFConnectionManager();
				connectionManager.connectLinkToWriter(OTFLinkAgentsHandler.Writer.class);
				connectionManager.connectWriterToReader(OTFLinkAgentsHandler.Writer.class, OTFLinkAgentsHandler.class);
				connectionManager.connectReaderToReceiver(OTFLinkAgentsHandler.class, OGLSimpleQuadDrawer.class);
				connectionManager.connectReceiverToLayer(OGLSimpleQuadDrawer.class, OGLSimpleStaticNetLayer.class);
				connectionManager.connectReaderToReceiver(OTFLinkAgentsHandler.class, AgentPointDrawer.class);
				connectionManager.connectReceiverToLayer(AgentPointDrawer.class, OGLAgentPointLayer.class);
				connectionManager.connectWriterToReader(OTFAgentsListHandler.Writer.class, OTFAgentsListHandler.class);
				connectionManager.connectReaderToReceiver(OTFAgentsListHandler.class, AgentPointDrawer.class);
				
				if (config.scenario().isUseTransit()) {
					connectionManager.connectWriterToReader(FacilityDrawer.Writer.class, FacilityDrawer.Reader.class);
					connectionManager.connectReaderToReceiver(FacilityDrawer.Reader.class, FacilityDrawer.DataDrawer.class);
					connectionManager.connectReceiverToLayer(FacilityDrawer.DataDrawer.class, SimpleSceneLayer.class);
				}
				
				if (config.scenario().isUseLanes() && (!config.scenario().isUseSignalSystems())) {
					connectionManager.connectWriterToReader(OTFLaneWriter.class, OTFLaneReader.class);
					connectionManager.connectReaderToReceiver(OTFLaneReader.class, OTFLaneSignalDrawer.class);
					connectionManager.connectReceiverToLayer(OTFLaneSignalDrawer.class, SimpleSceneLayer.class);
				} else if (config.scenario().isUseSignalSystems()) {
					connectionManager.connectWriterToReader(OTFSignalWriter.class, OTFSignalReader.class);
					connectionManager.connectReaderToReceiver(OTFSignalReader.class, OTFLaneSignalDrawer.class);
					connectionManager.connectReceiverToLayer(OTFLaneSignalDrawer.class, SimpleSceneLayer.class);
				}
				OTFClient otfClient = new OTFClient();
				otfClient.setServer(server);
				SettingsSaver saver = new SettingsSaver("otfsettings");
				OTFVisConfigGroup visconf = saver.tryToReadSettingsFile();
				if (visconf == null) {
					visconf = server.getOTFVisConfig();
				}
				OTFClientControl.getInstance().setOTFVisConfig(visconf); // has to be set before OTFClientQuadTree.getConstData() is invoked!
				OTFServerQuadTree serverQuadTree = server.getQuad(connectionManager);
				OTFClientQuadTree clientQuadTree = serverQuadTree.convertToClient(server, connectionManager);
				clientQuadTree.setConnectionManager(connectionManager);
				clientQuadTree.getConstData();
				OTFHostControlBar hostControlBar = otfClient.getHostControlBar();
				OTFOGLDrawer mainDrawer = new OTFOGLDrawer(clientQuadTree, hostControlBar, config.otfVis());
				OTFQueryControl queryControl = new OTFQueryControl(server, hostControlBar, visconf);
				OTFQueryControlToolBar queryControlBar = new OTFQueryControlToolBar(queryControl, visconf);
				queryControl.setQueryTextField(queryControlBar.getTextField());
				otfClient.getFrame().getContentPane().add(queryControlBar, BorderLayout.SOUTH);
				mainDrawer.setQueryHandler(queryControl);
				otfClient.addDrawerAndInitialize(mainDrawer, saver);
				if (config.otfVis().isMapOverlayMode()) {

					assertZoomLevel17(config);
					otfClient.addMapViewer(osmTileFactory());
				}
				otfClient.show();
			}
		});
	}
	
	private static void assertZoomLevel17(Config config) {
		if(config.otfVis().getMaximumZoom() != 17) {
			throw new RuntimeException("The OSM layer only works with maximumZoomLevel = 17. Please adjust your config.");
		}
	}
	
	private static TileFactory osmTileFactory() {
		final int max=17;
		TileFactoryInfo info = new TileFactoryInfo(0, 17, 17,
				256, true, true,
				"http://tile.openstreetmap.org",
				"x","y","z") {
			@Override
			public String getTileUrl(int x, int y, int zoom) {
				zoom = max-zoom;
				String url = this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
				return url;
			}

		};
		TileFactory tf = new DefaultTileFactory(info);
		return tf;
	}

}
