package playground.sergioo.singapore2012;

import org.matsim.core.controler.Controler;

public class RunControler {
	public static void main(String[] args) {
		Controler controler = new Controler(args);
		controler.setOverwriteFiles(true);
		controler.run();
	}
}