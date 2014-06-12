package org.matsim.contrib.parking.parkingChoice.carsharing;

import java.util.Collection;
import java.util.LinkedList;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.parking.lib.DebugLib;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkImpl;

public class DummyParkingModuleWithFreeFloatingCarSharing implements
		ParkingModuleWithFreeFloatingCarSharing {

	private Controler controler;
	private LinkedList<Id> availableVehicles;

	public DummyParkingModuleWithFreeFloatingCarSharing(Controler controler,
			Collection<ParkingCoordInfo> initialDesiredVehicleCoordinates) {
		this.controler = controler;

		availableVehicles = new LinkedList<Id>();

		for (ParkingCoordInfo parkInfo : initialDesiredVehicleCoordinates) {
			availableVehicles.add(parkInfo.getVehicleId());
		}
		// the vehicle will be parked at the closest free parking from desired
		// coordinate.
	}

	@Override
	public ParkingLinkInfo getNextFreeFloatingVehicle(Coord coord, Id personId) {
		Id vehicleId = null;
		if (availableVehicles.size() > 0) {
			vehicleId = availableVehicles.poll();
		} else {
			DebugLib.stopSystemAndReportInconsistency("no vehicle available");
		}

		NetworkImpl network = (NetworkImpl) controler.getNetwork();

		return new ParkingLinkInfo(vehicleId, network.getNearestLink(coord)
				.getId());
	}

	@Override
	public ParkingLinkInfo parkFreeFloatingVehicle(Id vehicleId, Coord destCoord, Id personId) {
		availableVehicles.add(vehicleId);
		NetworkImpl network = (NetworkImpl) controler.getNetwork();
		return new ParkingLinkInfo(vehicleId, network.getNearestLink(destCoord)
				.getId());
	}

	@Override
	public void resetForNewIterationStart() {
		// TODO Auto-generated method stub
		
	}

}
