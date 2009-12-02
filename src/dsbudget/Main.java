package dsbudget;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
	
	static public String version = "2.0.10";
	
    public static Embedded tomcat = null;
    private Host host = null;
    static String page_url = null;
    
    public static TrayIcon trayIcon;
	
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
			main.createTrayIcon();
		} catch (LifecycleException e) {
			System.out.println(e.toString());
		}
		System.out.println("Opening a browser...");
		page_url = "http://localhost:"+conf.getProperty("tomcat_port")+"/dsbudget/main";
		BrowserControl.displayURL(page_url);
	}
	
	public void createTrayIcon()
	{
		if (SystemTray.isSupported()) {

		    SystemTray tray = SystemTray.getSystemTray();
		    Image image = Toolkit.getDefaultToolkit().getImage("dsbudget.png");

		    /*
		    MouseListener mouseListener = new MouseListener() {
		        public void mouseClicked(MouseEvent e) {
		    		BrowserControl.displayURL(page_url);
		        }

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
		    };
			*/
		            
		    PopupMenu popup = new PopupMenu();
		    
		    MenuItem start = new MenuItem("Open dsBudget");
		    start.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            System.out.println("Opening dsBudget...");
		    		BrowserControl.displayURL(page_url);
		        }
		    });
		    popup.add(start);    
		    
		    MenuItem exit = new MenuItem("Exit");
		    exit.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            System.out.println("Stopping...");
		            try {
						tomcat.stop();
						Budget.savethread.requestStop();
						Budget.savethread.join();
					} catch (LifecycleException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InterruptedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
		            System.out.println("Exiting...");
		            System.exit(0);
		        }
		    });
		    popup.add(exit);

		    trayIcon = new TrayIcon(image, "dsBudget", popup);
		    
		    ActionListener actionListener = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            System.out.println("Opening dsBudget...");
		    		BrowserControl.displayURL(page_url);
		    		/*
		            trayIcon.displayMessage("Action Event", 
		                "An Action Event Has Been Performed!",
		                TrayIcon.MessageType.INFO);
		                */
		        }
		    };
		            
		    trayIcon.setImageAutoSize(true);
		    trayIcon.addActionListener(actionListener);
		    //trayIcon.addMouseListener(mouseListener);

		    try {
		        tray.add(trayIcon);
		    } catch (AWTException e) {
		        System.err.println("TrayIcon could not be added.");
		    }

		} else {
			//tray icon not supported... what can I do?
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
