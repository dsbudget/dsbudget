package dsbudget;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import dsbudget.model.Budget;

public class SaveThread extends Thread {
	static Logger logger = Logger.getLogger(Main.class);
	
	private volatile boolean terminateRequested = false;
	private volatile boolean saveRequested = false;
	private Budget budget;
	
	public SaveThread(Budget it) {
		budget = it;
	}
	
    public void run() {
    	while(!terminateRequested) {
    		try {
    			while (saveRequested) {
	    			saveRequested = false;
	    			budget.saveXML(Main.conf.getProperty("document"));
	    			logger.info("Saved document");
    			}
    			sleep(1000);
    		} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Failed to save document.\n" + e.getMessage());
    			logger.error("Failed to save document: " + e.getMessage());
    		}
    	}
    }
    public void requestStop() {
    	terminateRequested = true;
    }
    
    public void save() {
    	saveRequested = true;
    }
}