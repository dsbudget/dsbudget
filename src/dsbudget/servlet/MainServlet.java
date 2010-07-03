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

import com.divrep.DivRepContainer;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepPage;

import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepButton.Style;

import dsbudget.Main;
import dsbudget.SaveThread.FileUpdateCallBack;
import dsbudget.i18n.Labels;
import dsbudget.model.Page;
import dsbudget.servlet.ServletBase;
import dsbudget.view.MainView;
import dsbudget.view.PageDialog;
import dsbudget.view.RemovePageDialog;

public class MainServlet extends ServletBase  {
	
	//DivRepSelectBox pageselector;
	DivRepButton pagesettingsbutton;
	DivRepButton removepagebutton;
	
	DivRepButton newpagebutton;
	
	RemovePageDialog removepagedialog;
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
			current_page = budget.findPage(Integer.parseInt(request.getParameter("page")));
		} else {
			//then use document's openpage
			current_page = budget.findPage(budget.openpage);
		}
		
		//if we can't find, let's use the first page
		if(current_page == null) {
			if(budget.pages.size() > 0) {
				current_page = budget.pages.get(0);
			} else {
				//we have no page whatsoever - create one
				current_page = new Page(budget);
				current_page.name = Labels.getString(M_LABEL_UNTITLED);
				budget.pages.add(current_page);
			}
		}
		budget.openpage = current_page.name;
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		decidePageToOpen(request);
		
		//Initialize DivRep components
		new DivRepContainer(request) {
			public void initPage(final DivRepPage pageroot) {
				pagedialog = new PageDialog(pageroot, budget, current_page);
				removepagedialog = new RemovePageDialog(pageroot, budget, current_page);
				pageview = new MainView(pageroot, budget, current_page);
				
				LinkedHashMap<Integer, String> pages_kv = new LinkedHashMap<Integer, String>();
				
				pagesettingsbutton = new DivRepButton(pageroot, Labels.getString(M_LABEL_SETTINGS));
				pagesettingsbutton.setStyle(DivRepButton.Style.ALINK);
				pagesettingsbutton.setToolTip(Labels.getString(M_LABEL_EDIT_CURRENT_PAGE_SETTINGS));
				pagesettingsbutton.addEventListener(new DivRepEventListener(){
					public void handleEvent(DivRepEvent e) {
						pagedialog.open(false);
					}
				});
				
				removepagebutton = new DivRepButton(pageroot, Labels.getString(M_LABEL_REMOVE));
				removepagebutton.setStyle(DivRepButton.Style.ALINK);
				removepagebutton.setToolTip(Labels.getString(M_LABEL_REMOVE_OPENED_PAGE));
				removepagebutton.addEventListener(new DivRepEventListener(){
					public void handleEvent(DivRepEvent e) {
						removepagedialog.open();
					}
				});
				
				newpagebutton = new DivRepButton(pageroot, Labels.getString(M_LABEL_NEW_PAGE));
		        newpagebutton.setStyle(Style.ALINK);
		        newpagebutton.addEventListener(new DivRepEventListener() {
					public void handleEvent(DivRepEvent e) {
						pagedialog.open(true);
					}});
		        
		    	budget.savethread.setFileUpdateCallBack(new FileUpdateCallBack() {
		    		public void alert() {
		    			pageroot.alert("Document has been updated externally - reloading document");
		    			loadBudgetDocument();
		    			pageroot.js("window.location.reload();");
		    		}
		    	});
			}
		};
	
		PrintWriter out = response.getWriter();
		renderHeader(out, request);
		renderSide(out, request);
		renderMain(out, request);
		renderFooter(out, request);
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
			if(p == current_page) {
				out.write("<div class=\"page currentpage\">"+p.name);
			} else {
				out.write("<div class=\"page\" onclick=\"document.location='"+"?page="+p.getID()+"';\">"+p.name);				
			}
			out.write("</div>");
		}
		out.write("<br/></div>");
		
		out.write("</div>");
	}
	
	void renderMain(PrintWriter out, HttpServletRequest request)
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
	
	public static final String M_LABEL_SETTINGS = "Main.LABEL_SETTINGS";
	public static final String M_LABEL_EDIT_CURRENT_PAGE_SETTINGS = "Main.LABEL_EDIT_CURRENT_PAGE_SETTINGS";
	public static final String M_LABEL_REMOVE = "Main.LABEL_REMOVE";
	public static final String M_LABEL_REMOVE_OPENED_PAGE = "Main.LABEL_REMOVE_OPENED_PAGE";
	public static final String M_LABEL_NEW_PAGE = "Main.LABEL_NEW_PAGE";
	public static final String M_LABEL_UNTITLED = "Main.LABEL_UNTITLED";

}
