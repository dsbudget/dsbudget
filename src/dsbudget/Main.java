package dsbudget;

import java.net.InetAddress;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.startup.Embedded;
import org.apache.tomcat.util.IntrospectionUtils;

public class Main {
	
	private String path = null;
    private Embedded embedded = null;
    private Host host = null;
    private Context rootcontext;

    String port = "6091";

	public static void main(String[] args) {
		Main main = new Main();
		try {
			main.startTomcat();
		} catch (LifecycleException e) {
			//if I can't start the server, maybe there is another one already running..
			//let it continue..
			e.printStackTrace();
		}
		BrowserControl.displayURL("http://localhost:"+main.port+"/dsbudget/main");
	}
		
    public void startTomcat() throws LifecycleException {
        Engine engine = null;

        System.setProperty("catalina.base", "tomcat");
        
        // Create an embedded server
        embedded = new Embedded();
        embedded.setCatalinaHome("tomcat");

        // set the memory realm
        MemoryRealm memRealm = new MemoryRealm();
        embedded.setRealm(memRealm);

        // Create an engine
        engine = embedded.createEngine();
        engine.setDefaultHost("localhost");

        // Create a default virtual host
        host = embedded.createHost("localhost", "webapps");
        engine.addChild(host);
  
        // Create the ROOT context
        rootcontext = embedded.createContext("", "ROOT");
        rootcontext.setReloadable(true);
        rootcontext.addWelcomeFile("index.jsp");
        host.addChild(rootcontext);
		
        // create another application Context
        /*
        Context appCtx = this.embedded.createContext("/manager", "manager");
        appCtx.setPrivileged(true); 
        this.host.addChild(appCtx);
        */
        
        Context appCtx = embedded.createContext("/dsbudget", "dsbudget");
        appCtx.setPrivileged(true); 
        host.addChild(appCtx);
        
        // Install the assembled container hierarchy
        embedded.addEngine(engine);
        String addr = null;
        Connector connector = null;
        InetAddress address = null;
        try {
            connector = new Connector();
            connector.setSecure(false);
            //address = InetAddress.getLocalHost();
            if (address != null) {
                IntrospectionUtils.setProperty(connector, "address", "localhost");
            }
            IntrospectionUtils.setProperty(connector, "port", port);     
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        connector.setEnableLookups(false);

        embedded.addConnector(connector);
        embedded.start();
    }
    
    public void stopTomcat() throws Exception {
        embedded.stop();
    }
}
