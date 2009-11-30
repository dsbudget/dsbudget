package dsbudget.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepSelectBox;
import dsbudget.model.Page;
import dsbudget.servlet.ServletBase;
import dsbudget.view.MainView;
import dsbudget.view.PageDialog;
import dsbudget.view.RemoveDialog;

public class MainServlet extends ServletBase  {
	
	DivRepSelectBox pageselector;
	DivRepButton pagesettingsbutton;
	DivRepButton removepagebutton;
	
	RemoveDialog removedialog;
	PageDialog pagedialog;
	
	MainView pageview;
	
    public MainServlet() {
        super();
    }
    
	protected void decidePageToOpen(HttpServletRequest request)
	{
		//decide which page to display
		//URL page parameter takes precedence
		if(request.getParameter("page") != null) {
			page = budget.findPage(Integer.parseInt(request.getParameter("page")));
		} else {
			//then use document's openpage
			page = budget.findPage(budget.openpage);
		}
		
		//if we can't find, let's use the first page
		if(page == null) {
			if(budget.pages.size() > 0) {
				page = budget.pages.get(0);
			} else {
				//we have no page whatsoever - create one
				page = new Page(budget);
				page.name = "Untitled";
				budget.pages.add(page);
			}
		}
		budget.openpage = page.name;
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		decidePageToOpen(request);
		
		pageroot = DivRepRoot.initPageRoot(request);
		initControls();
		
		PrintWriter out = response.getWriter();
		renderHeader(out, request);
		renderContent(out, request);
		renderFooter(out, request);
	}
	
	//setup all DivRep controls
	protected void initControls()
	{
		initPageControl();
		pagedialog = new PageDialog(pageroot, budget, page) {
			public void onCancel() {
				pageselector.setValue(page.getID());
				pageselector.redraw();
				pagedialog.close();
			}
		};
		removedialog = new RemoveDialog(pageroot, budget, page);
		
		pageview = new MainView(pageroot, budget, page);
	}
	
	protected void initPageControl()
	{
		LinkedHashMap<Integer, String> pages_kv = new LinkedHashMap<Integer, String>();
		
		//populate key/value for page selector and instantiate
		for(Page page : budget.pages) {
			pages_kv.put(page.getID(), page.name);
		}
		pages_kv.put(-1, "(Create New Page)");
		pageselector = new DivRepSelectBox(pageroot, pages_kv);
		pageselector.addClass("inline");
		pageselector.setHasNull(false);
		pageselector.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				for(Page page : budget.pages) {
					if(page.getID().equals(Integer.parseInt(e.value)))  {
						pageselector.redirect("?page="+page.getID());
						budget.save();
						return;
					}
				}
				pagedialog.open(true);
			}});
		if(page != null) {
			pageselector.setValue(page.getID());
		}
		pagesettingsbutton = new DivRepButton(pageroot, "Settings");
		pagesettingsbutton.setStyle(DivRepButton.Style.ALINK);
		pagesettingsbutton.setToolTip("Edit settings for currently opened page");
		pagesettingsbutton.addEventListener(new DivRepEventListener(){
			public void handleEvent(DivRepEvent e) {
				pagedialog.open(false);
			}
		});
		
		removepagebutton = new DivRepButton(pageroot, "Remove");
		removepagebutton.setStyle(DivRepButton.Style.ALINK);
		removepagebutton.setToolTip("Remove currently opened page");
		removepagebutton.addEventListener(new DivRepEventListener(){
			public void handleEvent(DivRepEvent e) {
				removedialog.open();
			}
		});
        
	}
	
	void renderContent(PrintWriter out, HttpServletRequest request)
	{	
		out.write("<table class=\"controls\"><tr>");

		out.write("<td class=\"pageselector\">");
		
		pagesettingsbutton.render(out);
		removepagebutton.render(out);
		
		pageselector.render(out);
		out.write("</td>");
		
		out.write("</tr></table>");
		
		out.write("<div id=\"main\">");
		pageview.render(out);
		out.write("</div>");
		
		pagedialog.render(out);
		removedialog.render(out);
	}
}
