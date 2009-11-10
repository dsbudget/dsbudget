package dsbudget.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepPage;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextBox;
import com.divrep.common.DivRepButton.Style;

import dsbudget.model.Page;
import dsbudget.servlet.ServletBase;
import dsbudget.view.BudgetingView;
import dsbudget.view.DivRepDialog;
import dsbudget.view.ExpenseView;
import dsbudget.view.IncomeView;
import dsbudget.view.MainView;

public class MainServlet extends ServletBase  {
	
	DivRepSelectBox pageselector;
	DivRepButton newpagebutton;
	NewPageDialog newpage_dialog;
	MainView pageview;
	
	class NewPageDialog extends DivRepDialog
	{
		DivRepTextBox title;
		DivRepSelectBox copy_from;
		
		public NewPageDialog(DivRep parent) {
			super(parent, true);
			setTitle("Create New Page");
			
			Date d = new Date();
			SimpleDateFormat df = (SimpleDateFormat) SimpleDateFormat.getInstance();
			df.applyPattern("M y");
			String newname = df.format(new Date());
			
			title = new DivRepTextBox(this);
			title.setLabel("Title");
			title.setWidth(200);
			title.setValue(newname);
			
			LinkedHashMap<Integer, String> pages_kv = new LinkedHashMap<Integer, String>();
			for(Page page : budget.pages) {
				pages_kv.put(page.getID(), page.name);
			}
			copy_from = new DivRepSelectBox(this, pages_kv);
			copy_from.setNullLabel("(Empty Page)");
			copy_from.setLabel("Copy from");
			copy_from.setValue(pageview.getPageID());
		}

		public void onCancel() {
			close();
		}

		@Override
		public void onSubmit() {
			// TODO Auto-generated method stub
			
		}

		public void renderDialog(PrintWriter out) {
			title.render(out);
			copy_from.render(out);
		}
		
	}
	
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
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		decidePageToOpen(request);
		
		DivRepPage pageroot = DivRepRoot.initPageRoot(request);
		initControls(pageroot);
		
		PrintWriter out = response.getWriter();
		renderHeader(pageroot, out, request);
		renderContent(pageroot, out, request);
		renderFooter(pageroot, out, request);
	}
	
	//setup all DivRep controls
	protected void initControls(DivRepPage pageroot)
	{
		initPageControl(pageroot);
		pageview = new MainView(pageroot, budget, page);
	}
	
	
	protected void initPageControl(DivRepPage pageroot)
	{
		LinkedHashMap<Integer, String> pages_kv = new LinkedHashMap<Integer, String>();
		
		//populate key/value for page selector and instantiate
		for(Page page : budget.pages) {
			pages_kv.put(page.getID(), page.name);
		}
		pageselector = new DivRepSelectBox(pageroot, pages_kv);
		pageselector.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				for(Page page : budget.pages) {
					if(page.getID().equals(Integer.parseInt(e.value)))  {
						pageselector.redirect("?page="+page.getID());
						return;
					}
				}
			}});
		if(page != null) {
			pageselector.setValue(page.getID());
		}
        newpagebutton = new DivRepButton(pageroot, "Create New Page");
        newpagebutton.setStyle(Style.ALINK);
        newpagebutton.addEventListener(new DivRepEventListener(){
			public void handleEvent(DivRepEvent e) {
				newpage_dialog.open();
			}
		});
        newpage_dialog = new NewPageDialog(pageroot);
	}
	
	void renderContent(DivRepPage pageroot, PrintWriter out, HttpServletRequest request)
	{	
		
		out.write("<div class=\"pageselector\">");
		pageselector.render(out);
		newpagebutton.render(out);
		out.write("</div>");
		
		out.write("<div id=\"main\">");
		pageview.render(out);
		out.write("</div>");
		newpage_dialog.render(out);
	}
}
