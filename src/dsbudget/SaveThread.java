package dsbudget;

import dsbudget.model.Budget;

public class SaveThread extends Thread {
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
	    			System.out.println("saving..");
	    			budget.saveXML(Main.conf.getProperty("document"));
	    			System.out.println("Saved");
    			}
    			sleep(1000);
    		} catch (Exception e) {
    			System.out.println("Failed to save document: " + e.toString());
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