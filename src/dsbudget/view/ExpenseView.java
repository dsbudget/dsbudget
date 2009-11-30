package dsbudget.view;

import java.awt.Color;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepDate;
import com.divrep.common.DivRepTextBox;
import com.divrep.common.DivRepToggler;

import dsbudget.model.Category;
import dsbudget.model.Expense;
import dsbudget.model.Page;

public class ExpenseView extends DivRep {
	MainView mainview;

	ArrayList<CategoryView> category_views;
	
	NumberFormat nf = NumberFormat.getCurrencyInstance();
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
	
	class CategoryBalanceGraphView extends DivRep
	{
		Category category;
		Boolean hidden = false;
		
		public CategoryBalanceGraphView(DivRep parent, Category _category) {
			super(parent);
			category = _category;
		}

		@Override
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
				out.write("<img src=\"chart?type=balance&pageid="+mainview.getPageID()+"&catid="+category.getID()+"&time="+current.getTime()+"\"/>");
			}
			out.write("</div>");
		}	
	}

	class CategoryView extends DivRep 
	{
		Category category;
		DivRepButton graph_toggler;
		CategoryBalanceGraphView graph;
		DivRepButton addnewexpense;
		
		private void setGraphTogglerTitle()
		{
			if(graph.isHidden()) {
				graph_toggler.setTitle("Show Balance Graph");
			} else {
				graph_toggler.setTitle("Hide Balance Graph");					
			}	
		}
		
		public CategoryView(DivRep parent, Category _category) {
			super(parent);
			category = _category;
			
			graph = new CategoryBalanceGraphView(this, category);
			graph.setHidden(category.hide_graph);
			graph_toggler = new DivRepButton(this, "");
			setGraphTogglerTitle();

			graph_toggler.setStyle(DivRepButton.Style.ALINK);
			graph_toggler.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					graph.setHidden(!graph.isHidden());
					category.hide_graph = graph.isHidden();
					graph.redraw();
					setGraphTogglerTitle();
					graph_toggler.redraw();
					mainview.save();
				}
			});
			
			addnewexpense = new DivRepButton(this, "Add New Expense");
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
	 			for(Expense expense : category.getExpensesSortByDate()) {
					if(expense.toString().equals(e.value)) {
						mainview.removeExpense(category, expense);
						mainview.save();
			 			return;
					}
	 			}
			} else {
				//edit
	 			for(Expense expense : category.getExpensesSortByDate()) {
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
			
			out.write("<tr style=\"background-color: #"+String.format("%06x", (header_color.getRGB() & 0x00ffffff) )+";\" class=\"expense_category\">");
			out.write("<th width=\"20px\"></th><th width=\"270px\">"+StringEscapeUtils.escapeHtml(category.name)+"</th>");
			out.write("<td>"+StringEscapeUtils.escapeHtml(category.description)+"</td>");
			out.write("<th width=\"100px\"></th><th width=\"90px\" class=\"note\" style=\"text-align: right;\">"+nf.format(category.amount)+"</th><td width=\"20px\"></td>");
			out.write("</tr>");

			for(Expense expense : category.getExpensesSortByDate()) {
				out.write("<tr class=\"expense\" onclick=\"divrep('"+getNodeID()+"', event, '"+expense.toString()+"')\">");
				out.write("<th>&nbsp;</th>"); //side
				out.write("<td>"+StringEscapeUtils.escapeHtml(expense.where)+"&nbsp;</td>");
				out.write("<td>"+StringEscapeUtils.escapeHtml(expense.description)+"</td>");
				out.write("<td style=\"text-align: right;\">"+df.format(expense.date)+"</td>");
				String negative = "";
				if(expense.amount.compareTo(BigDecimal.ZERO) < 0) {
					negative = "negative";
				}
				out.write("<td style=\"text-align: right;\" class=\""+negative+"\">");
				/*
				if(expense.amount.compareTo(BigDecimal.ZERO) > 0) {
					out.write("- ");
				}
				*/
				out.write(nf.format(expense.amount)+"</td>");
				
				out.write("<td>");
				out.write("<img onclick=\"divrep('"+getNodeID()+"', event, '"+expense.toString()+"', 'remove');\" class=\"remove_button\" src=\"css/images/delete.png\"/>");
				out.write("</td>");
				out.write("</tr>");
			}
			
			//balance
			BigDecimal remain = category.amount;
			remain = remain.subtract(category.getTotalExpense());
			out.write("<tr class=\"expense_footer\">");
			
			out.write("<td></td>");
			
			out.write("<td class=\"newitem\">");
			addnewexpense.render(out);
			out.write("</td>");
			
			out.write("<td style=\"text-align: right;\">");
			graph_toggler.render(out);
			out.write("</td>"); //desc
			
			out.write("<th style=\"text-align: right;\">Remaining</th>");
			String negative = "";
			if(remain.compareTo(BigDecimal.ZERO) < 0) {
				negative = "negative";
			}
			out.write("<th style=\"text-align: right;\" class=\""+negative+"\">"+nf.format(remain)+"</th>");
			
			out.write("<td></td>"); //remove button
			
			out.write("</tr>");
			
			out.write("</table>");
		
			graph.render(out);
		
			out.write("</div>");
		}
	}
	
	public ExpenseView(final MainView parent) {
		super(parent);
		mainview = parent;
		
		initView();
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
		out.write("<h2>Expense</h2>");
		for(CategoryView view : category_views) {
			view.render(out);
		}
		out.write("</div>");
	}

}
