package dsbudget.servlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

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
		
		out.write("<div class=\"pagename\">" + current_page.name + "</div>");
		
		pageview.render(out);
		
		out.write("</div>"); //main
		
		pagedialog.render(out);
		removepagedialog.render(out);
	}

	@Override
	protected void initDivRepObjects(DivRepPage pageroot) {
		pageview = new MainView(pageroot, budget, current_page);
	}
	

}
