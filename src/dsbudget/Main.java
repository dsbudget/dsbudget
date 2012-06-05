package dsbudget;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SplashScreen;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.IntrospectionUtils;

import dsbudget.model.Budget;
import dsbudget.model.Page;

public class Main {
	static Logger logger = Logger.getLogger(Main.class);
	
	static public String version = "2.0.24";
	
    public static Embedded tomcat = null;
    private Host host = null;
    static String page_url = null;
    
    public static TrayIcon trayIcon;
	
    public static Properties conf;

	public static void main(String[] args) {
		Main main = new Main();
		logger.info("Starting dsBudget server " + Main.version);
		
		conf = new Properties();
		try {
			//load configuration
			File conf_file = new File("dsbudget.conf");
			if(!conf_file.exists()) {
				JOptionPane.showMessageDialog(null, "Can't find dsbudget.conf. Make sure you are using a luncher shortcut which sets the current directory, or run .exe on a directory that contains the conf file.");	
				System.exit(1);
			}
			conf.load(new FileInputStream(conf_file));
			
			//override it with user configuration -- TODO - I should let user overide the location via in put parameter
			File user_conf_file = new File("dsbudget.user.conf");
			if(user_conf_file.exists()) {
				Properties user_conf = new Properties();
				user_conf.load(new FileInputStream(user_conf_file));
				for(Object key_obj : user_conf.keySet()) {
					String key = (String)key_obj;
					String value = (String)user_conf.get(key);
					conf.setProperty(key, value);
				}
			}

			//configuration overrides
			String document_override = System.getProperty("document");
			if(document_override != null) {
				logger.info("Overriding document path: " + document_override);
				Main.conf.setProperty("document", document_override);
			}
			
			//start server
			try {					
				main.startTomcat();
				main.createTrayIcon();
			} catch (LifecycleException e) {
				logger.error(e);
			} 
	
			//open browser
			page_url = "http://"+conf.getProperty("tomcat_ip")+":"+conf.getProperty("tomcat_port")+"/dsbudget/main";
			if (Desktop.isDesktopSupported()) {
				logger.info("Opening a browser...");
				try {
					Desktop.getDesktop().browse(new URI(page_url));
				} catch (URISyntaxException e) {
					JOptionPane.showMessageDialog(null, "Failed to open browser: " + e);	
					e.printStackTrace();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Failed to open browser: " + e);	
					e.printStackTrace();
				}
				
				//close splash screen
				SplashScreen splash = SplashScreen.getSplashScreen();
				if(splash != null) {
					splash.close();
				}
			}
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(null, "Failed to load dsbudget.conf: " + e1);	
			logger.error(e1);
			System.exit(1);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "Failed to load dsbudget.conf: " + e1);	
			logger.error(e1);
			System.exit(1);
		}
	}
	
	public void createTrayIcon()
	{
		if (SystemTray.isSupported()) {

		    SystemTray tray = SystemTray.getSystemTray();
		    Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("trayicon.png"));
		            
		    PopupMenu popup = new PopupMenu();
		    
		    MenuItem start = new MenuItem("Open dsBudget");
		    start.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		    		if (Desktop.isDesktopSupported()) {
		    	
		    			try {
				            logger.info("Opening dsBudget...");
		    				Desktop.getDesktop().browse(new URI(page_url));
		    			} catch (IOException e2) {
		    				logger.error(e2);
		    			} catch (URISyntaxException e3) {
		    				logger.error(e3);
		    			}
		    		}
		        }
		    });
		    popup.add(start);    
		    
		    MenuItem exit = new MenuItem("Exit");
		    exit.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	stop();
		        }
		    });
		    popup.add(exit);

		    trayIcon = new TrayIcon(image, "dsBudget", popup);
		    
		    ActionListener actionListener = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		    		if (Desktop.isDesktopSupported()) {
				    	
		    			try {
				            logger.info("Opening dsBudget... at " + page_url);
		    				Desktop.getDesktop().browse(new URI(page_url));
		    			} catch (IOException e2) {
		    				logger.error("Failed to opne browser: ", e2);
		    			} catch (URISyntaxException e3) {
		    				logger.error("Failed to opne browser: ", e3);
		    			}
		    		}
		        }
		    };
		            
		    trayIcon.setImageAutoSize(true);
		    trayIcon.addActionListener(actionListener);
		    
		    try {
		        tray.add(trayIcon);
		    } catch (AWTException e) {
		        logger.error("TrayIcon could not be added : ", e);
		    }

		} else {
			logger.error("Tray icon is not supported..");
		}
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
            IntrospectionUtils.setProperty(connector, "address", conf.getProperty("tomcat_ip"));
            IntrospectionUtils.setProperty(connector, "port", conf.getProperty("tomcat_port"));     
        } catch (Exception ex) {
            logger.error("Couldn't create connector", ex);
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
    
    static public void stop() {
        logger.info("Stopping...");
        try {
			tomcat.stop();
			Budget.savethread.requestStop();
			Budget.savethread.join();
		} catch (LifecycleException e1) {
			logger.error(e1);
		} catch (InterruptedException e2) {
			logger.error(e2);
		}
        logger.info("Exiting...");
        System.exit(0);
    }
}
