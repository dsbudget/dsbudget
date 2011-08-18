package dsbudget.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

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
import dsbudget.view.PageDialog;
import dsbudget.view.RemovePageDialog;

public abstract class PageServletBase  extends BudgetServletBase {
	
	protected abstract void renderMain(PrintWriter out, HttpServletRequest request);
	protected abstract void initDivRepObjects(DivRepPage pageroot);
	
	DivRepButton pagesettingsbutton;
	DivRepButton removepagebutton;
	RemovePageDialog removepagedialog;
	PageDialog pagedialog;
	DivRepButton newpagebutton;
	protected Page current_page; 
	
	protected void renderHeader(PrintWriter out, HttpServletRequest request) 
	{	
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n\t\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		out.println("<html><head>");
		out.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />");
		if(current_page == null) {
			out.println("<title>dsBudget</title>");
		} else {
			out.println("<title>"+StringEscapeUtils.escapeHtml(current_page.name)+"</title>");
		}
		
		out.println("<link href=\"css/smoothness/jquery-ui-1.8.2.custom.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.println("<link href=\"css/divrep.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.println("<link href=\"css/dsbudget.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"css/dsbudget.print.css\" />");
		
		out.println("<script type=\"text/javascript\" src=\"jquery-1.4.2.min.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"jquery-ui-1.8.2.custom.min.js\"></script>");
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
		out.println(" | ");
		out.println("<a href=\"http://sites.google.com/site/dsbudgethome/\" target=\"_blank\">Homepage</a>");
		out.println(" | ");
		out.println("<a href=\"http://code.google.com/p/dsbudget/issues/list\" target=\"_blank\">Report Bugs</a>");
		out.println(" | ");
		out.println("<a href=\"https://sites.google.com/site/dsbudgethome/donate\" target=\"_blank\">Donate</a>");
		
		out.println("<br>");
		File document = new File(Main.conf.getProperty("document"));
		document.getAbsolutePath();
		out.println(StringEscapeUtils.escapeHtml(document.getAbsolutePath()));
		
		out.println("</div>");
		out.println("</body></html>");
	}
	
	void renderSide(PrintWriter out, HttpServletRequest request)
	{
		out.write("<div id=\"side\">");
		
		///////////////////////////////////////////////////////////////////////////////////////////
		// Budget Pages
		out.write("<div class=\"pageselector\">");

		out.write("<div class=\"newpage\">");
		newpagebutton.render(out);
		out.write("</div>");
		
		out.write("<h2>"+Labels.getString("Main.LABEL_PAGES")+"</h2>");

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
				out.write("<div class=\"page currentpage\">"+p.name+"</div>");
			} else {
				out.write("<div class=\"page\" onclick=\"document.location='"+"main?page="+p.getID()+"';\">"+p.name+"</div>");				
			}
		}
		out.write("<br/></div>");
		
		///////////////////////////////////////////////////////////////////////////////////////////
		// Reports
		out.write("<div class=\"pageselector\">");
		out.write("<br/>");
		//out.write("<h2>"+Labels.getString("Main.LABEL_REPORTS")+"</h2>");
		if(Page.search_page == current_page) {
			out.write("<div class=\"page currentpage\">"+Page.search_page.name+"</div>");
		} else {
			out.write("<div class=\"page\" onclick=\"document.location='search'\">"+Labels.getString("Search.LABEL_PAGE_NAME")+"</div>");
		}
		out.write("<br/></div>");
		
		out.write("</div>");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		decidePageToOpen(request);
		
		//Initialize DivRep components
		new DivRepContainer(request) {
			public void initPage(final DivRepPage pageroot) {
				pagedialog = new PageDialog(pageroot, budget, current_page);
				removepagedialog = new RemovePageDialog(pageroot, budget, current_page);
				initDivRepObjects(pageroot);
				
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
	
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		renderHeader(out, request);
		renderSide(out, request);
		renderMain(out, request);
		renderFooter(out, request);
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
	
	public static final String M_LABEL_SETTINGS = "Main.LABEL_SETTINGS";
	public static final String M_LABEL_EDIT_CURRENT_PAGE_SETTINGS = "Main.LABEL_EDIT_CURRENT_PAGE_SETTINGS";
	public static final String M_LABEL_REMOVE = "Main.LABEL_REMOVE";
	public static final String M_LABEL_REMOVE_OPENED_PAGE = "Main.LABEL_REMOVE_OPENED_PAGE";
	public static final String M_LABEL_NEW_PAGE = "Main.LABEL_NEW_PAGE";
	public static final String M_LABEL_UNTITLED = "Main.LABEL_UNTITLED";
}
