/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
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
 * *********************************************************************** *
 */

package org.matsim.contrib.drt.optimizer.rebalancing.adaptiveRealTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.analysis.DrtRequestAnalyzer;
import org.matsim.contrib.drt.analysis.zonal.DrtGridUtils;
import org.matsim.contrib.drt.analysis.zonal.DrtZonalSystem;
import org.matsim.contrib.drt.analysis.zonal.DrtZonalWaitTimesAnalyzer;
import org.matsim.contrib.drt.analysis.zonal.ZonalIdleVehicleXYVisualiser;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingParams;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.drt.optimizer.rebalancing.mincostflow.AggregatedMinCostRelocationCalculator;
import org.matsim.contrib.drt.optimizer.rebalancing.mincostflow.MinCostRelocationCalculator;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeQSimModule;
import org.matsim.core.controler.MatsimServices;
import org.matsim.utils.gis.shp2matsim.ShpGeometryUtils;

/**
 * @author michalm, Chengqi Lu
 */
public class DrtModeAdapativeRealTimeRebalanceModule extends AbstractDvrpModeModule {
	private static final Logger log = Logger.getLogger(DrtModeAdapativeRealTimeRebalanceModule.class);
	private final DrtConfigGroup drtCfg;
	
	public DrtModeAdapativeRealTimeRebalanceModule(DrtConfigGroup drtCfg) {
		super(drtCfg.getMode());
		this.drtCfg = drtCfg;
	}

	@Override
	public void install() {
		log.info("Adaptive Real Time Rebalancing Algorithm is now being installed!");
		RebalancingParams generalParams = drtCfg.getRebalancingParams().orElseThrow();
		bindModal(DrtZonalSystem.class).toProvider(modalProvider(getter -> {

			if (generalParams.getRebalancingZonesGeneration()
					.equals(RebalancingParams.RebalancingZoneGeneration.ShapeFile)) {
				final List<PreparedGeometry> preparedGeometries = ShpGeometryUtils
						.loadPreparedGeometries(generalParams.getRebalancingZonesShapeFileURL(getConfig().getContext()));
				Map<String, Geometry> zones = new HashMap<>();
				for (int i = 0; i < preparedGeometries.size(); i++) {
					zones.put("" + (i + 1), preparedGeometries.get(i).getGeometry());
				}
				return new DrtZonalSystem(getter.getModal(Network.class), zones);
			}

			if (drtCfg.getOperationalScheme() == DrtConfigGroup.OperationalScheme.serviceAreaBased) {
				final List<PreparedGeometry> preparedGeometries = ShpGeometryUtils
						.loadPreparedGeometries(drtCfg.getDrtServiceAreaShapeFileURL(getConfig().getContext()));
				Network modalNetwork = getter.getModal(Network.class);
				Map<String, Geometry> zones = DrtGridUtils.createGridFromNetworkWithinServiceArea(modalNetwork,
						generalParams.getCellSize(), preparedGeometries);
				return new DrtZonalSystem(modalNetwork, zones);
			}
			return new DrtZonalSystem(getter.getModal(Network.class), generalParams.getCellSize());
		})).asEagerSingleton();

		installQSimModule(new AbstractDvrpModeQSimModule(getMode()) {
			@Override
			protected void configureQSim() {
				bindModal(RebalancingStrategy.class).toProvider(modalProvider(
						getter -> new AdaptiveRealTimeRebalncingStrategy(getter.getModal(DrtZonalSystem.class),
								getter.getModal(Fleet.class), getter.getModal(MinCostRelocationCalculator.class),
								generalParams, getter.getModal(InactiveZoneIdentifier.class))))
						.asEagerSingleton();

				bindModal(MinCostRelocationCalculator.class)
						.toProvider(modalProvider(getter -> new AggregatedMinCostRelocationCalculator(
								getter.getModal(DrtZonalSystem.class), getter.getModal(Network.class))))
						.asEagerSingleton();
			}
		});
		
		//Create Inactive Zone remover (this will be created only once)
		bindModal(InactiveZoneIdentifier.class).toProvider(modalProvider(
				getter -> new InactiveZoneIdentifier(getter.getModal(DrtZonalSystem.class))))
				.asEagerSingleton();
		
		// binding the event handler 
		addEventHandlerBinding().to(modalKey(InactiveZoneIdentifier.class));
		

		{
			//this is rather analysis - but depends on DrtZonalSystem so it can not be moved into DrtModeAnalysisModule until DrtZonalSystem at the moment...
			bindModal(ZonalIdleVehicleXYVisualiser.class).
					toProvider(modalProvider(
							getter -> new ZonalIdleVehicleXYVisualiser(getter.get(MatsimServices.class),
									drtCfg.getMode(), getter.getModal(DrtZonalSystem.class)))).asEagerSingleton();
			addControlerListenerBinding().to(modalKey(ZonalIdleVehicleXYVisualiser.class));
			addEventHandlerBinding().to(modalKey(ZonalIdleVehicleXYVisualiser.class));

			bindModal(DrtZonalWaitTimesAnalyzer.class).toProvider(modalProvider(
					getter -> new DrtZonalWaitTimesAnalyzer(drtCfg, getter.getModal(DrtRequestAnalyzer.class),
							getter.getModal(DrtZonalSystem.class)))).asEagerSingleton();
			addControlerListenerBinding().to(modalKey(DrtZonalWaitTimesAnalyzer.class));
			addEventHandlerBinding().to(modalKey(DrtZonalWaitTimesAnalyzer.class));
		}

	}
}
