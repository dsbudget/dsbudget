package dsbudget.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepButton.Style;

import dsbudget.Main;
import dsbudget.model.Page;
import dsbudget.servlet.ServletBase;
import dsbudget.view.MainView;
import dsbudget.view.PageDialog;
import dsbudget.view.RemoveDialog;

public class MainServlet extends ServletBase  {
	
	//DivRepSelectBox pageselector;
	DivRepButton pagesettingsbutton;
	DivRepButton removepagebutton;
	
	DivRepButton newpagebutton;
	
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
		renderSide(out, request);
		renderMain(out, request);
		renderFooter(out, request);
	}
	
	//setup all DivRep controls
	protected void initControls()
	{
		initPageControl();
		pagedialog = new PageDialog(pageroot, budget, page) {
			public void onCancel() {
				pagedialog.close();
			}
		};
		removedialog = new RemoveDialog(pageroot, budget, page);
		
		pageview = new MainView(pageroot, budget, page);
	}
	
	protected void initPageControl()
	{
		LinkedHashMap<Integer, String> pages_kv = new LinkedHashMap<Integer, String>();
	
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
		
		newpagebutton = new DivRepButton(pageroot, "New Page");
        newpagebutton.setStyle(Style.ALINK);
        newpagebutton.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				pagedialog.open(true);
			}});
	}
	
	void renderSide(PrintWriter out, HttpServletRequest request)
	{
		out.write("<div id=\"side\">");
		
		out.write("<div class=\"pageselector\">");

		out.write("<div class=\"newpage\">");
		newpagebutton.render(out);
		out.write("</div>");
		
		out.write("<h2>Pages</h2>");

		ArrayList<Page> sorted_pages = budget.pages;
		if(Main.conf.getProperty("pagelist_sortorder").equals("up")) {
			Collections.sort(sorted_pages, new Comparator<Page>() {
				public int compare(Page o1, Page o2) {
					return(o2.created.compareTo(o1.created));
				}});			
		} else {
			Collections.sort(sorted_pages, new Comparator<Page>() {
				public int compare(Page o1, Page o2) {
					return(o1.created.compareTo(o2.created));
				}});
		}

		
		for(Page p : budget.pages) {
			if(p == page) {
				out.write("<div class=\"page currentpage\">"+p.name);
			} else {
				out.write("<div class=\"page\" onclick=\"document.location='"+"?page="+p.getID()+"';\">"+p.name);				
			}
			/*
			//remove button
			out.write("<span style=\"float: right;\">");
			RemoveButton button = new RemoveButton(pageroot);
			button.render(out);
			out.write("</span>");
			*/
			out.write("</div>");
		}
		out.write("<br/></div>");
		
		out.write("</div>");
	}
	
	class RemoveButton extends DivRep
	{

		public RemoveButton(DivRep parent) {
			super(parent);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void render(PrintWriter out) {
			out.write("<img onclick=\"divrep('"+getNodeID()+"', event, 'whatever', 'remove');\" class=\"remove_button\" src=\"css/images/delete.png\"/>");			
		}
		
	}
	
	void renderMain(PrintWriter out, HttpServletRequest request)
	{					
		out.write("<div id=\"main\">");
		
		out.write("<div class=\"pagecontrol\">");
		pagesettingsbutton.render(out);
		out.write("&nbsp;&nbsp;&nbsp;");
		removepagebutton.render(out);
		out.write("</div>");
		
		out.write("<div class=\"pagename\">" + page.name + "</div>");
		
		pageview.render(out);
		
		out.write("</div>");
		
		pagedialog.render(out);
		removedialog.render(out);
	}
}
