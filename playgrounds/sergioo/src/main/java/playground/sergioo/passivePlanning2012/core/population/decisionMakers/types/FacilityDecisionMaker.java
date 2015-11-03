package playground.sergioo.passivePlanning2012.core.population.decisionMakers.types;

import org.matsim.core.api.experimental.facilities.ActivityFacility;

public interface FacilityDecisionMaker extends DecisionMaker {

	//Methods
	public void setTypeOfActivity(String typeOfActivity);
	public ActivityFacility decideFacility();

}