package dsbudget.view;

import java.awt.Color;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepButton.Style;

import dsbudget.i18n.Labels;
import dsbudget.model.Category;
import dsbudget.model.Expense;
import dsbudget.model.Page;

public class ExpenseView extends DivRep {
	
	MainView mainview;
	DivRepButton toggler;
	ArrayList<CategoryView> category_views;
	
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	NumberFormat pnf = NumberFormat.getPercentInstance();
	DateFormat df = DateFormat.getDateInstance();

	class PageBalanceGraphView extends DivRep
	{
		Page page;
		Boolean hidden = false;
		
		public PageBalanceGraphView(DivRep parent, Page _page) {
			super(parent);
			page = _page;
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		public Boolean isHidden() { return hidden; }
		public void setHidden(Boolean flag) { hidden = flag; }
		
		public void render(PrintWriter out) {
			out.write("<div class=\"graph\" id=\""+getNodeID()+"\">");
			if(!hidden) {
				Date current = new Date();
				//time is to force reload when this divrep is refreshed
				out.write("<img src=\"chart?type=pagebalance&pageid="+page.getID()+"\"/>");
			}
			out.write("</div>");
		}	
	}
	
	abstract class CategoryGraphView extends DivRep {
		Category category;
		Boolean hidden = false;
		
		public Boolean isHidden() { return hidden; }
		public void setHidden(Boolean flag) { hidden = flag; }
		
		public CategoryGraphView(DivRep parent, Category category) {
			super(parent);
			this.category = category;
		}
		
		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class CategoryBalanceGraphView extends CategoryGraphView
	{
		public CategoryBalanceGraphView(DivRep parent, Category category) {
			super(parent, category);
		}
		public void render(PrintWriter out) {
			out.write("<div class=\"graph\" id=\""+getNodeID()+"\">");
			if(!hidden) {
				Date current = new Date();
				//time is to force reload when this divrep is refreshed
				out.write("<img alt=\"line graph\" src=\"chart?type=balance&amp;pageid="+mainview.getPageID()+"&amp;catid="+category.getID()+"&amp;time="+current.getTime()+"\"/>");
			}
			out.write("</div>");
		}	
	}

	class CategoryPieGraphView extends CategoryGraphView
	{	
		public CategoryPieGraphView(DivRep parent, Category category) {
			super(parent, category);
		}
		
		public void render(PrintWriter out) {
			out.write("<div class=\"graph\" id=\""+getNodeID()+"\">");
			if(!hidden) {
				Date current = new Date();
				//time is to force reload when this divrep is refreshed
				out.write("<img alt=\"pie graph\" src=\"chart?type=pie&amp;pageid="+mainview.getPageID()+"&amp;catid="+category.getID()+"&amp;time="+current.getTime()+"\"/>");
			}
			out.write("</div>");
		}	
	}
	
	class CategoryView extends DivRep 
	{
		Category category;
		
		DivRepButton balance_graph_toggler;
		CategoryGraphView balance_graph;
		
		DivRepButton pie_graph_toggler;
		CategoryGraphView pie_graph;
		
		DivRepButton addnewexpense;
		
		private void setBalanceGraphTogglerTitle()
		{
			if(balance_graph.isHidden()) {
				//balance_graph_toggler.setTitle(Labels.getString(EXV_LABEL_SHOW_BALANCE_GRAPH));
				balance_graph_toggler.setTitle("css/images/chart_balance_off.png");
			} else {	
				//balance_graph_toggler.setTitle(Labels.getString(EXV_LABEL_HIDE_BALANCE_GRAPH));	
				balance_graph_toggler.setTitle("css/images/chart_balance.png");
			}	
		}
		
		private void setPieGraphTogglerTitle()
		{
			if(pie_graph.isHidden()) {
				//pie_graph_toggler.setTitle(Labels.getString("ExpenseView.LABEL_SHOW_PIE_GRAPH"));
				pie_graph_toggler.setTitle("css/images/chart_pie_off.png");
			} else {	
				//pie_graph_toggler.setTitle(Labels.getString("ExpenseView.LABEL_HIDE_PIE_GRAPH"));	
				pie_graph_toggler.setTitle("css/images/chart_pie.png");
			}	
		}
	
		public CategoryView(DivRep parent, Category _category) {
			super(parent);
			category = _category;
			
			balance_graph = new CategoryBalanceGraphView(this, category);
			balance_graph.setHidden(category.hide_balance_graph);
			balance_graph_toggler = new DivRepButton(this, "");
			balance_graph_toggler.setStyle(Style.IMAGE);
			balance_graph_toggler.setToolTip(Labels.getString("ExpenseView.LABEL_TIP_BALANCE_GRAPH"));
			setBalanceGraphTogglerTitle();
			balance_graph_toggler.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					balance_graph.setHidden(!balance_graph.isHidden());
					category.hide_balance_graph = balance_graph.isHidden();
					balance_graph.redraw();
					setBalanceGraphTogglerTitle();
					balance_graph_toggler.redraw();
					mainview.save();
				}
			});
			
			pie_graph = new CategoryPieGraphView(this, category);
			pie_graph.setHidden(category.hide_pie_graph);
			pie_graph_toggler = new DivRepButton(this, "");
			setPieGraphTogglerTitle();
			pie_graph_toggler.setStyle(Style.IMAGE);
			pie_graph_toggler.setToolTip(Labels.getString("ExpenseView.LABEL_TIP_PIE_GRAPH"));
			pie_graph_toggler.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					pie_graph.setHidden(!pie_graph.isHidden());
					category.hide_pie_graph = pie_graph.isHidden();
					pie_graph.redraw();
					setPieGraphTogglerTitle();
					pie_graph_toggler.redraw();
					mainview.save();
				}
			});

			
			addnewexpense = new DivRepButton(this, Labels.getString(EXV_LABEL_ADD_NEW_EXPENSE));
			addnewexpense.setStyle(DivRepButton.Style.ALINK);
			addnewexpense.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					mainview.expense_dialog.open(category, null);
				}
			});
		}

		protected void onEvent(DivRepEvent e) {
			if(e.action.equals("remove")) {
				//remove
	 			for(Expense expense : category.getExpenses()) {
					if(expense.toString().equals(e.value)) {
						mainview.removeExpense(category, expense);
						mainview.save();
			 			return;
					}
	 			}
			} if(e.action.equals("cat_edit")) {
	 			for(Category category : mainview.getCategories()) {
					if(category.toString().equals(e.value)) {
						mainview.category_dialog.open(category);
			 			return;
					}
	 			}
			} else {
				//edit expense
	 			for(Expense expense : category.getExpenses()) {
					if(expense.toString().equals(e.value)) {
						mainview.expense_dialog.open(category, expense);
						return;
					}
	 			}
			}
		}
	
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"expense_category_container\">");
			out.write("<table width=\"100%\">");
			
			Color orig = category.color;
			int r = (255 + 255 + orig.getRed())/3;
			int g = (255 + 255 + orig.getGreen())/3;
			int b = (255 + 255 + orig.getBlue())/3;
			
			Color header_color = new Color(r,g,b);
			
			out.write("<tr style=\"background-color: #"+String.format("%06x", (header_color.getRGB() & 0x00ffffff) )+";\"");
			out.write(" onclick=\"divrep('"+getNodeID()+"', event, '"+category.toString()+"', 'cat_edit')\" class=\"expense_category\">");
			out.write("<th width=\"20px\"></th><th width=\"280px\">"+StringEscapeUtils.escapeHtml(category.name)+"</th>");
			out.write("<td>"+StringEscapeUtils.escapeHtml(category.description)+"</td>");
			out.write("<th width=\"100px\"></th><th width=\"110px\" class=\"note\" style=\"text-align: right;\">");
			if(category.isPercentage()) {
				out.write("<span class=\"note\">(");
				out.write(StringEscapeUtils.escapeHtml((pnf.format(category.getPercentage()))));
				out.write(")</span> ");
			}
			out.write(StringEscapeUtils.escapeHtml(nf.format(category.getAmount())));
			out.write("</th><td width=\"20px\"></td>");
			out.write("</tr>");
			
			for(Expense expense : category.getExpensesSorted()) {
				String expense_type = "";
				String decoration = "";
				if(expense.tentative) {
					expense_type += " tentative";
					decoration += "<b>(" + Labels.getHtmlEscapedString("ExpenseView.LABEL_EXPENSE_SCHEDULED") + ")</b>";
				}
				if(expense.recurring) {
					expense_type += " expense-recurring";
					decoration += "<b>(" + Labels.getHtmlEscapedString("ExpenseView.LABEL_EXPENSE_RECURRING") + ")</b>";
				}
				out.write("<tr class=\"expense "+expense_type+"\" onclick=\"divrep('"+getNodeID()+"', event, '"+expense.toString()+"')\">");
				out.write("<th>&nbsp;</th>"); //side
				out.write("<td>"+StringEscapeUtils.escapeHtml(expense.where)+"&nbsp;" + decoration + "</td>");
				out.write("<td>"+StringEscapeUtils.escapeHtml(expense.description)+"</td>");
				out.write("<td style=\"text-align: right;\">"+StringEscapeUtils.escapeHtml(df.format(expense.date))+"</td>");
				
				out.write("<td style=\"text-align: right;\">");
				AmountView av_deduction = new AmountView(this, expense.amount);
				av_deduction.render(out);
				out.write("<td>");
				
				out.write("<img onclick=\"divrep('"+getNodeID()+"', event, '"+expense.toString()+"', 'remove');\" class=\"remove_button\" alt=\"remove\" src=\"css/images/delete.png\"/>");
				out.write("</td>");
				out.write("</tr>");
			}
			
			//balance
			BigDecimal remain = category.getAmount().subtract(category.getTotalExpense());
			out.write("<tr class=\"expense_footer\">");
			
			out.write("<td></td>");
			
			out.write("<td class=\"newitem\">");
			addnewexpense.render(out);
			out.write("</td>");
			
			out.write("<td style=\"text-align: right;\">");
			pie_graph_toggler.render(out);
			out.write("&nbsp;&nbsp;&nbsp;");
			balance_graph_toggler.render(out);
			out.write("</td>"); //desc
			
			out.write("<th style=\"text-align: right;\">");
			out.write(Labels.getHtmlEscapedString(EXV_LABEL_REMAINING));
			out.write("</th>");
			
			out.write("<th style=\"text-align: right;\">");
			AmountView av_remain = new AmountView(this, remain);
			av_remain.render(out);
			out.write("</th>");
			
			out.write("<td></td>"); //remove button
			
			out.write("</tr>");
			
			//scheduled remaining
			BigDecimal total_scheduled = category.getTotalScheduled();
			if(!total_scheduled.equals(BigDecimal.ZERO)) {
				BigDecimal scheduled_remaining = remain.subtract(total_scheduled);
				out.write("<tr class=\"expense_footer\">");
				
				out.write("<td></td>");
				out.write("<td class=\"newitem\"></td>");
				out.write("<th colspan=\"2\" style=\"text-align: right;\">");
				out.write(Labels.getHtmlEscapedString(EXV_LABEL_SCHEDULED_REMAINING));
				out.write("</th>");

				out.write("<th style=\"text-align: right;\">");
				AmountView av_scheduled_remaining = new AmountView(this, scheduled_remaining);
				av_scheduled_remaining.render(out);
				out.write("</th>");
				out.write("<td></td>"); //remove button
				
				out.write("</tr>");
			}
			
			out.write("</table>");
		
			pie_graph.render(out);
			balance_graph.render(out);
		
			out.write("</div>");
		}
	}
		
	public ExpenseView(final MainView parent) {
		super(parent);
		mainview = parent;
		
		toggler = new DivRepButton(this, "");
		toggler.setStyle(DivRepButton.Style.IMAGE);
		setTogglerIcon();
		toggler.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				mainview.page.hide_expense = !mainview.page.hide_expense;
				setTogglerIcon();
				redraw();
				mainview.save();
			}
		});

		initView();
	}
	
	protected void setTogglerIcon()
	{
		if(mainview.page.hide_expense) {
			toggler.setTitle("css/images/expand.gif");
			toggler.setToolTip("Expand Expense View");
		} else {
			toggler.setTitle("css/images/collapse.gif");	
			toggler.setToolTip("Collapse Expense View");
		}
	}
	
	
	public void initView() 
	{
		category_views = new ArrayList<CategoryView>();
		for(Category category : mainview.getCategories()) {	
			category_views.add(new CategoryView(this, category));
		}
	}
	

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
	}
	
	public void updateExpenseCategory(Category category) {
		for(CategoryView view : category_views) {
			if(view.category == category) {
				view.redraw();
				return;
			}
		}
	}

	public void render(PrintWriter out) {
		out.write("<div class=\"expenseview round8\" id=\""+getNodeID()+"\">");
		out.write("<table width=\"100%\"><tr>");
		out.write("<th><h2>");
		out.write(Labels.getHtmlEscapedString(EXV_LABEL_HEADER));
		out.write("</h2></th>");
		out.write("<th width=\"20px\">");
		toggler.render(out);
		out.write("</th>");
		out.write("</tr></table>");
		if(!mainview.page.hide_expense) {
			for(CategoryView view : category_views) {
				view.render(out);
			}
		}
		out.write("</div>");
	}

	public static final String EXV_LABEL_HEADER = "ExpenseView.LABEL_HEADER";
	public static final String EXV_LABEL_ADD_NEW_EXPENSE = "ExpenseView.LABEL_ADD_NEW_EXPENSE";
	public static final String EXV_LABEL_IMPORT_EXPENSES = "ExpenseView.LABEL_ADD_NEW_EXPENSES";
	public static final String EXV_LABEL_REMAINING = "ExpenseView.LABEL_REMAINING";
	public static final String EXV_LABEL_SCHEDULED_REMAINING = "ExpenseView.LABEL_SCHEDULED_REMAINING";

}
