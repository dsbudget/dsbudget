package dsbudget.view;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepButton.Style;

import dsbudget.Main;
import dsbudget.i18n.Labels;
import dsbudget.model.Budget;
import dsbudget.model.Page;

public class PageSelector extends DivRep {
	private static final long serialVersionUID = 7856662946401482263L;
	
	DivRepButton newpagebutton;
	PageDialog pagedialog;
	Budget budget;
	Page current_page;
	
	public PageSelector(DivRep parent, Budget budget, Page current_page, final PageDialog pagedialog) {
		super(parent);
		this.pagedialog = pagedialog;
		this.budget = budget;
		this.current_page = current_page;
		
		newpagebutton = new DivRepButton(this, Labels.getString("Main.LABEL_NEW_PAGE"));
        newpagebutton.setStyle(Style.ALINK);
        newpagebutton.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				pagedialog.open(true);
			}});
	}

	@Override
	public void render(PrintWriter out) {
		///////////////////////////////////////////////////////////////////////////////////////////
		// Budget Pages
		out.write("<div id=\""+getNodeID()+"\" class=\"pageselector\">");

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
			String cls = "";
			if(p.getBalance().compareTo(BigDecimal.ZERO) < 0) {
				cls = "page-negativebalance";
			}
			if(p == current_page) {
				out.write("<div class=\"page currentpage "+cls+"\">"+p.name+"</div>");
			} else {
				out.write("<div class=\"page "+cls+"\" onclick=\"document.location='"+"main?page="+p.getID()+"';\">"+p.name+"</div>");				
			}
		}
		out.write("<br/></div>");//pageselector
	}

	@Override
	protected void onEvent(DivRepEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}