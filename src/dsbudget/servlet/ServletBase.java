package dsbudget.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

// URLEncoder.encode(page.name, "UTF-8")

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepPage;
import com.divrep.common.DivRepSelectBox;

import dsbudget.model.Budget;
import dsbudget.model.Page;
import dsbudget.view.DivRepColorPicker;

public class ServletBase extends HttpServlet {
	Budget budget;
	Page page; //current page
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		budget = (Budget)config.getServletContext().getAttribute("budget");
		if(budget == null) {
			log("Loading Budget Document for the first time");
			budget = Budget.loadXML("C:/dev/java/dsbudget/BudgetDocument.xml");
			config.getServletContext().setAttribute("budget", budget);
			log("Loaded " + budget.pages.size() + " pages");
		}
	}

	protected void renderHeader(DivRepPage pageroot, PrintWriter out, HttpServletRequest request) 
	{	
		out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n\t\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		out.write("<html><head>");
		out.write("<title>"+StringEscapeUtils.escapeHtml(page.name)+"</title>");
		out.write("<link href=\"css/smoothness/jquery-ui-1.7.2.custom.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		//out.write("<link href=\"colorpicker/css/colorpicker.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.write("<link href=\"css/divrep.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.write("<link href=\"css/dsbudget.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		
		out.write("<script type=\"text/javascript\" src=\"jquery-1.3.2.min.js\"></script>");
		out.write("<script type=\"text/javascript\" src=\"jquery-ui-1.7.2.custom.min.js\"></script>");
		//out.write("<script type=\"text/javascript\" src=\"colorpicker/js/colorpicker.js\"></script>");
		out.write("<script type=\"text/javascript\" src=\"divrep.js\"></script>");
		
		DivRepColorPicker.renderInit(out);
		
		out.write("</head>");
		out.write("<body>");
		
		out.write("<div id=\"header\"><h1>dsBudget</h1><h2>Formally known as SimpleD Budget</h2></div>");
		out.write("<div id=\"content\">");	
	
	}
	protected void renderFooter(DivRepPage pageroot, PrintWriter out, HttpServletRequest request) 
	{
		out.write("</div>");	
		out.write("<div class=\"footer\">");
		renderDonateButton(out);
		out.write("</div>");
		out.write("</body></html>");
	}
	private void renderDonateButton(PrintWriter out) 
	{
		out.write("<form action=\"https://www.paypal.com/cgi-bin/webscr\" method=\"post\">");
		out.write("<input type=\"hidden\" name=\"cmd\" value=\"_s-xclick\">");
		out.write("<input type=\"hidden\" name=\"hosted_button_id\" value=\"9574026\">");
		out.write("If you like this software, you can help us improve it by donating to this project.");
		out.write("<input type=\"image\" src=\"https://www.paypal.com/en_US/i/btn/btn_donate_SM.gif\" border=\"0\" name=\"submit\" alt=\"PayPal - The safer, easier way to pay online!\">");
		out.write("<img alt=\"\" border=\"0\" src=\"https://www.paypal.com/en_US/i/scr/pixel.gif\" width=\"1\" height=\"1\">");
		out.write("</form>");
	}
}
