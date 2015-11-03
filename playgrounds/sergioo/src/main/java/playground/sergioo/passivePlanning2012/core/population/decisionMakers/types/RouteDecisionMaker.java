package playground.sergioo.passivePlanning2012.core.population.decisionMakers.types;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;

public interface RouteDecisionMaker extends DecisionMaker {

	//Methods
	public void setLeg(Leg leg);
	public void setStartLink(Link startLink);
	public void setEndLink(Link endLink);
	public Leg decideRoute();

}