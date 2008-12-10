package org.matsim.locationchoice;

import org.matsim.controler.Controler;
import org.matsim.gbl.Gbl;
import org.matsim.testcases.MatsimTestCase;


public class RandomLocationMutatorTest  extends MatsimTestCase {
	
	private RandomLocationMutator randomlocationmutator = null;
	private Controler controler = null;
	
	public RandomLocationMutatorTest() {
	}
	
	private void initialize() {
		Gbl.reset();
		String path = "test/input/org/matsim/locationchoice/config.xml";		
		String configpath[] = {path};
		controler = new Controler(configpath);
		controler.setOverwriteFiles(true);
		controler.run();		
		this.randomlocationmutator = new RandomLocationMutator(controler.getNetwork(), controler);
	}

	/* 
	 * TODO: Construct scenario with knowledge to compare plans before and after loc. choice
	 */
	public void testHandlePlan() {
		this.initialize();
		this.randomlocationmutator.handlePlan(controler.getPopulation().getPerson("1").getSelectedPlan());	
	}	
}