package dsbudget.servlet;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;

import dsbudget.Main;
import dsbudget.model.Budget;
import dsbudget.model.Page;

public class ServletBase extends HttpServlet {
	static Logger logger = Logger.getLogger(ServletBase.class);
	
	DivRep pageroot;
	Budget budget;
	Page page; //current page
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		Date today = new Date();
		
		budget = (Budget)config.getServletContext().getAttribute("budget");
		if(budget == null) {
			String path = Main.conf.getProperty("document");
			log("Loading Budget Document at: " + path);
			try {
				//load the document
				budget = Budget.loadXML(new File(path));
				
				//create a backup
				try {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss");
					String backup_path = path + ".backup." + format.format(today);
					logger.info("Backing up the current document to " + backup_path);
					copy(path, backup_path);
				} catch (Exception e) {
					logger.error("Failed to create a backup: " + e.toString());
				}
				
			} catch (Exception e) {
				
				JOptionPane.showMessageDialog(null, "Failed to open document.\n" + e.getMessage() + "\nCreating an empty document.");
							
				logger.error("Failed to load XML " + path + " -- " + e.toString());
				logger.error("Creaing empty doc");
				
				budget = new Budget();
				Page page = Main.createEmptyPage(budget);
				budget.pages.add(page);
			}
		
			//remove old backups
			long keep_backup_for = 1000*3600*24*Long.parseLong(Main.conf.getProperty("keep_backup_for").trim());
			File parent = new File(".");
			String[] files = parent.list();
			for(String file : files) {
				if(file.startsWith(path+".backup.")) {
					File bfile = new File(file);
					if(today.getTime() - bfile.lastModified() > keep_backup_for) {
						logger.info("Removing old backup file: " + bfile.toString());
						bfile.delete();
					}
				}
			}
			
			config.getServletContext().setAttribute("budget", budget);
			logger.info("Loaded " + budget.pages.size() + " pages");
		}
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

	protected void renderHeader(PrintWriter out, HttpServletRequest request) 
	{	
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n\t\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		out.println("<html><head>");
		out.println("<title>"+StringEscapeUtils.escapeHtml(page.name)+"</title>");

		out.println("<link href=\"css/smoothness/jquery-ui-1.7.2.custom.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.println("<link href=\"css/divrep.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.println("<link href=\"css/dsbudget.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"css/dsbudget.print.css\" />");
		
		out.println("<script type=\"text/javascript\" src=\"jquery-1.3.2.min.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"jquery-ui-1.7.2.custom.min.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"divrep.js\"></script>");
		
		out.println("<script type=\"text/javascript\" src=\"dsbudget.js\"></script>");
		
		out.println("</head>");
		out.println("<body>");
		
		out.println("<div id=\"header\"><span class=\"application_header\">dsBudget</span>");
		out.println("<span class=\"application_subheader\">"+StringEscapeUtils.escapeHtml(Main.conf.getProperty("subheader"))+"</span></div>");
		out.println("<div id=\"content\">");	
	}
	
	protected void renderFooter(PrintWriter out, HttpServletRequest request) 
	{
		out.println("</div>"); //end of content

		out.println("<div id=\"footer\">");
		out.println("<span class=\"version\">dsBudget "+Main.version+"</span>&nbsp;");
		out.println("<span class=\"divrep\">Developed with <a href=\"http://divrep.com\" target=\"_blank\">DivRep Framework</a> by <a href=\"http://sites.google.com/site/soichih/\" target=\"_blank\">Soichi Hayashi</a></span>");
		out.println("<br/>");
		out.println("<a href=\"http://sites.google.com/site/dsbudgethome/\" target=\"_blank\">Homepage</a>");
		out.println(" | ");
		out.println("<a href=\"http://dsbudget.blogspot.com/\" target=\"_blank\">Blog</a>");
		out.println(" | ");
		out.println("<a href=\"http://code.google.com/p/dsbudget/issues/list\" target=\"_blank\">Report Bugs</a>");
		out.println(" | ");
		out.println("<a href=\"http://groups.google.com/group/dsbudget/topics\" target=\"_blank\">Discussion Forum</a>");

		out.println("</div>");
		out.println("</body></html>");
	}

}
