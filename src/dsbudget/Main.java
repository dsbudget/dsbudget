package dsbudget;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.Properties;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.startup.Embedded;
import org.apache.tomcat.util.IntrospectionUtils;

import dsbudget.model.Budget;
import dsbudget.model.Page;

public class Main {
	
	static public String version = "2.0.8";
	
    public static Embedded tomcat = null;
    private Host host = null;

    public static Properties conf;

	public static void main(String[] args) {
		Main main = new Main();
		System.out.println("Starting dsBudget server " + Main.version);
		
		conf = new Properties();
		try {
			conf.load(new FileInputStream("dsbudget.conf"));
		} catch (FileNotFoundException e1) {
			System.out.println(e1.toString());
			System.exit(1);
		} catch (IOException e1) {
			System.out.println(e1.toString());
			System.exit(1);
		}
		
		//configuration overrides
		String document_override = System.getProperty("document");
		if(document_override != null) {
			System.out.println("Overriding document path: " + document_override);
			Main.conf.setProperty("document", document_override);
		}
		
		try {
			main.startTomcat();
		} catch (LifecycleException e) {
			System.out.println(e.toString());
			System.exit(1);
		}
		System.out.println("Opening a browser...");
		BrowserControl.displayURL("http://localhost:"+conf.getProperty("tomcat_port")+"/dsbudget/main");
	}
		
    public void startTomcat() throws LifecycleException {
        Engine engine = null;

        System.setProperty("catalina.base", "tomcat");
        
        // Create an embedded server
        tomcat = new Embedded();
        tomcat.setCatalinaHome("tomcat");
        
        // Create an engine
        engine = tomcat.createEngine();
        engine.setDefaultHost("localhost");

        // Create a default virtual host
        host = tomcat.createHost("localhost", "webapps");
        host.setAutoDeploy(false);
        engine.addChild(host);
        
        Context appCtx = tomcat.createContext("/dsbudget", "dsbudget");
        appCtx.setPrivileged(true); 
        host.addChild(appCtx);
        
        // Install the assembled container hierarchy
        tomcat.addEngine(engine);
        Connector connector = null;
        try {
            connector = new Connector();
            IntrospectionUtils.setProperty(connector, "address", "127.0.0.1");
            IntrospectionUtils.setProperty(connector, "port", conf.getProperty("tomcat_port"));     
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        connector.setEnableLookups(false);

        tomcat.addConnector(connector);
        tomcat.start();
    }
    
    public void stopTomcat() throws Exception {
    	tomcat.stop();
    }
    
    static public Page createEmptyPage(Budget budget) {
		Page page = new Page(budget);
		return page;
    }
}
