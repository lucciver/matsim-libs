package playground.mzilske.freight.events;

import playground.mzilske.freight.carrier.Shipment;

public interface ShipmentStatusHandler {
	
	public void shipmentPickedUp(Shipment shipment, double time);

	public void shipmentDelivered(Shipment shipment, double time);

}
