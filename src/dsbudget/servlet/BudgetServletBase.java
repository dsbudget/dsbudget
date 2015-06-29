package dsbudget.servlet;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.divrep.DivRep;
import com.divrep.DivRepContainer;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepPage;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepButton.Style;

import dsbudget.Main;
import dsbudget.SaveThread.FileUpdateCallBack;
import dsbudget.i18n.Labels;
import dsbudget.model.Budget;
import dsbudget.model.Page;
import dsbudget.view.MainView;
import dsbudget.view.PageDialog;
import dsbudget.view.RemovePageDialog;

abstract public class BudgetServletBase extends HttpServlet {
	static Logger logger = Logger.getLogger(BudgetServletBase.class);
	
	//DivRep pageroot;
	protected Budget budget = null;
	
	protected void loadBudgetDocument() {
		String path = Main.conf.getProperty("document");
		log("Loading Budget Document at: " + path);
		try {
			//load the document
			budget = Budget.loadXML(new File(path), budget);	
		} catch (Exception e) {
			
			JOptionPane.showMessageDialog(null, "Failed to open document.\n" + e.getMessage() + "\nCreating an empty document.");
						
			logger.error("Failed to load XML " + path + " -- " , e);
			logger.info("Creaing empty doc");
			
			budget = new Budget();
			Page page = Main.createEmptyPage(budget);
			budget.pages.add(page);
		}
			
		logger.info("Loaded " + budget.pages.size() + " pages");
	}
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
			
		budget = (Budget)config.getServletContext().getAttribute("budget");
		if(budget == null) {
			loadBudgetDocument();
			
			File path = new File(Main.conf.getProperty("document"));
			Date today = new Date();
			
			//create backup
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss");
				String backup_path = path + ".backup." + format.format(today);
				logger.info("Backing up the current document to " + backup_path);
				copy(path.getPath(), backup_path);
			} catch (Exception e) {
				logger.error("Failed to create a backup: ",e);
			}
			
			//remove old backups
			logger.info("keep_backup_for set to " + Main.conf.getProperty("keep_backup_for") + " days");
			long keep_backup_for = 1000*3600*24*Long.parseLong(Main.conf.getProperty("keep_backup_for").trim());
			File parent = path.getParentFile();
			if(parent == null) {
				parent = new File(".");
			}
			String[] files = parent.list();
			for(String file : files) {
				if(file.startsWith(path.getName()+".backup.")) {
					File bfile = new File(parent.toString() + File.separator + file.toString());
					if(today.getTime() - bfile.lastModified() > keep_backup_for) {
						logger.info("Removing old backup file: " + bfile.toString());
						bfile.delete();
					}
				}
			}
			config.getServletContext().setAttribute("budget", budget);
		}
		logger.info("completed init");
	}
	

	
	void copy(String source, String dest) throws IOException 
	{
	    FileReader in = new FileReader(source);
	    FileWriter out = new FileWriter(dest);
	    int c;

	    while ((c = in.read()) != -1)
	      out.write(c);

	    in.close();
	    out.close();
	}



}
