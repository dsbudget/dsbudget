package dsbudget.servlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRepPage;

import dsbudget.view.MainView;

public class MainServlet extends PageServletBase  {
	
	MainView pageview;
	
    public MainServlet() {
        super();
    }
    
	protected void renderMain(PrintWriter out, HttpServletRequest request)
	{					
		out.write("<div id=\"main\">");
		
		out.write("<div class=\"pagecontrol\">");
		pagesettingsbutton.render(out);
		out.write("&nbsp;&nbsp;&nbsp;");
		removepagebutton.render(out);
		out.write("</div>");
		
		out.write("<div class=\"pagename\">" + StringEscapeUtils.escapeHtml(current_page.name) + "</div>");
		
		if(current_page.description.length() > 0) {
			out.write("<p class=\"page-description\">" + StringEscapeUtils.escapeHtml(current_page.description) + "</p>");
		}
		
		pageview.render(out);
		
		out.write("</div>"); //main
		
		pagedialog.render(out);
		removepagedialog.render(out);
	}

	@Override
	protected void initDivRepObjects(DivRepPage pageroot) {
		pageview = new MainView(pageroot, budget, current_page, pageselector);
	}
}
