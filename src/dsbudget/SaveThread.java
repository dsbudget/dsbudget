package dsbudget;

import java.io.File;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import dsbudget.model.Budget;

public class SaveThread extends Thread {
	static Logger logger = Logger.getLogger(Main.class);
	private Budget budget;

	private volatile boolean terminateRequested = false;
    public void requestStop() {	terminateRequested = true; }
    
	private volatile boolean saveRequested = false;
    public void save() { saveRequested = true; }
	
	public interface FileUpdateCallBack {
		public void alert();
	}
	private FileUpdateCallBack callback = null;
	
	public SaveThread(Budget it) {
		budget = it;
	}
	public void setFileUpdateCallBack(FileUpdateCallBack callback) {
		this.callback = callback;
	}
	
	private long getTimestamp() {
    	File document = new File(Main.conf.getProperty("document"));	
		return document.lastModified();
	}
	
    public void run() {
		long timestamp = getTimestamp();
		
    	while(!terminateRequested) {
    		try {
    			while (saveRequested) {
	    			saveRequested = false;
	    			budget.saveXML(Main.conf.getProperty("document"));
	    			logger.info("Saved document");
	    	    
	    			timestamp = getTimestamp();
    			}
    		} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Failed to save document.\n" + e.getMessage());
    			logger.error("Failed to save document: ",e);
    		}
    		
    		try {
    			long new_timestamp = getTimestamp();
    			if(timestamp < new_timestamp) {
	    			logger.info("Someone outside updated the document..");
    				timestamp = new_timestamp;
    				if(callback != null) {
    					callback.alert();
    				} else {
    	    			logger.warn("No callback object to invoke!");
    				}
    			}
    		} catch(Exception e) {
				JOptionPane.showMessageDialog(null, "Failed to reload document.\n" + e.getMessage());
    			logger.error("Failed to reload document: ",e);
    		}
    		
			try {
				sleep(1000);
			} catch (InterruptedException e) {
    			logger.error("Interrupted while sleeping in SaveThread: ",e);
			}
    	}
    }
}