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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.catalina.LifecycleException;
import org.apache.commons.lang.StringEscapeUtils;
import com.divrep.DivRep;
import com.divrep.common.DivRepColorPicker;

import dsbudget.Main;
import dsbudget.model.Budget;
import dsbudget.model.Page;

public class ServletBase extends HttpServlet {
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
					System.out.println("Backing up the current document to " + backup_path);
					copy(path, backup_path);
				} catch (Exception e) {
					System.out.println("Failed to create a backup: " + e.toString());
				}
				
			} catch (Exception e) {
				System.out.println("Failed to load XML " + path + " -- " + e.toString());
				System.out.println("Creaing empty doc");
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
						System.out.println("Removing old backup file: " + bfile.toString());
						bfile.delete();
					}
				}
			}
			
			config.getServletContext().setAttribute("budget", budget);
			log("Loaded " + budget.pages.size() + " pages");
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
		out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n\t\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		out.write("<html><head>");
		out.write("<title>"+StringEscapeUtils.escapeHtml(page.name)+"</title>");

		out.write("<link href=\"css/smoothness/jquery-ui-1.7.2.custom.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.write("<link href=\"css/divrep.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.write("<link href=\"css/dsbudget.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.write("<link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"css/dsbudget.print.css\" />");
		
		out.write("<script type=\"text/javascript\" src=\"jquery-1.3.2.min.js\"></script>");
		out.write("<script type=\"text/javascript\" src=\"jquery-ui-1.7.2.custom.min.js\"></script>");
		out.write("<script type=\"text/javascript\" src=\"divrep.js\"></script>");
		
		out.write("<script type=\"text/javascript\" src=\"dsbudget.js\"></script>");
		
		out.write("</head>");
		out.write("<body>");
		
		out.write("<div id=\"header\"><h1>dsBudget</h1><h2>Previously known as SimpleD Budget</h2></div>");
		out.write("<div id=\"content\">");	
	}
	
	protected void renderFooter(PrintWriter out, HttpServletRequest request) 
	{
		out.write("</div>"); //end of content

		out.write("<div id=\"footer\">");
		out.write("<span class=\"version\">dsBudget "+Main.version+"</span>&nbsp;");
		out.write("<span class=\"divrep\">Developed with <a href=\"http://divrep.com\">DivRep Framework</a> by <a href=\"http://sites.google.com/site/soichih/\">Soichi Hayashi</a></span>");
		out.write("<br/>");
		out.write("<a href=\"http://sites.google.com/site/dsbudgethome/\" target=\"_blank\">Homepage</a>");
		out.write(" | ");
		out.write("<a href=\"http://dsbudget.blogspot.com/\" target=\"_blank\">Blog</a>");
		out.write(" | ");
		out.write("<a href=\"http://code.google.com/p/dsbudget/issues/list\" target=\"_blank\">Report Bugs</a>");
		out.write(" | ");
		out.write("<a href=\"http://groups.google.com/group/dsbudget/topics\" target=\"_blank\">Discussion Forum</a>");

		out.write("</div>");
		out.write("</body></html>");
	}

}
