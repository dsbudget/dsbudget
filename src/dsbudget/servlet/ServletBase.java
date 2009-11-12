package dsbudget.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.catalina.LifecycleException;
import org.apache.commons.lang.StringEscapeUtils;
import com.divrep.DivRep;

import dsbudget.Main;
import dsbudget.model.Budget;
import dsbudget.model.Page;
import dsbudget.view.DivRepColorPicker;

public class ServletBase extends HttpServlet {
	DivRep pageroot;
	Budget budget;
	Page page; //current page
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		budget = (Budget)config.getServletContext().getAttribute("budget");
		if(budget == null) {
			log("Loading Budget Document for the first time");
			try {
				String path = System.getProperty("document");
				if(path == null) {
					System.out.println("System parameter 'document' is not set (must be a path to the budget document XML). Trying default name.");
					path = "BudgetDocument.xml";
					System.setProperty("document", path);
				}
				budget = Budget.loadXML(path);
			} catch (Exception e) {
				System.out.println("Failed to load XML " + System.getProperty("document"));
				System.out.println("Creaing empty doc");
				budget = new Budget();
				Page page = new Page(budget);
				page.name = "Untitled";
				budget.pages.add(page);
			}
			config.getServletContext().setAttribute("budget", budget);
			log("Loaded " + budget.pages.size() + " pages");
		}
	}
	public void save() throws ParserConfigurationException, IOException, TransformerException {
		budget.saveXML(System.getProperty("document"));
	}

	protected void renderHeader(PrintWriter out, HttpServletRequest request) 
	{	
		out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n\t\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		out.write("<html><head>");
		out.write("<title>"+StringEscapeUtils.escapeHtml(page.name)+"</title>");
		out.write("<link href=\"css/smoothness/jquery-ui-1.7.2.custom.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		//out.write("<link href=\"colorpicker/css/colorpicker.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.write("<link href=\"css/divrep.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.write("<link href=\"css/dsbudget.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.write("<link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"css/dsbudget.print.css\" />");
		
		out.write("<script type=\"text/javascript\" src=\"jquery-1.3.2.min.js\"></script>");
		out.write("<script type=\"text/javascript\" src=\"jquery-ui-1.7.2.custom.min.js\"></script>");
		//out.write("<script type=\"text/javascript\" src=\"colorpicker/js/colorpicker.js\"></script>");
		out.write("<script type=\"text/javascript\" src=\"divrep.js\"></script>");
		
		DivRepColorPicker.renderInit(out);
		
		out.write("</head>");
		//out.write("<body onbeforeunload=\"if(confirm('Do you want to save the data?')) divrep('"+pageroot.getNodeID()+"', event, null, 'close')\">");
		out.write("<body>");
		
		out.write("<div id=\"header\"><h1>dsBudget</h1><h2>Formally known as SimpleD Budget</h2></div>");
		out.write("<div id=\"content\">");	
	
	}
	protected void renderFooter(PrintWriter out, HttpServletRequest request) 
	{
		out.write("</div>");	
		out.write("<div id=\"footer\">");
		out.write("<span class=\"version\">dsBudget v2.0.0a</span>&nbsp;");
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
