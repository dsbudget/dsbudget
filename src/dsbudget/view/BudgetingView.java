package dsbudget.view;

import java.text.NumberFormat;
import java.text.ParseException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepDate;
import com.divrep.common.DivRepSlider;
import com.divrep.common.DivRepTextBox;

import dsbudget.model.Category;
import dsbudget.model.Expense;
import dsbudget.model.Page;

public class BudgetingView extends DivRep {
	MainView mainview;

	LinkedHashMap<Category, DivRepSlider> sliders = new LinkedHashMap<Category, DivRepSlider>();
	DivRepButton addnewcategory;
	
	public BudgetingView(final MainView parent) {
		super(parent);
		mainview = parent;

		for(final Category category : mainview.getCategories()) {
			DivRepSlider slider = new DivRepSlider(this);
			slider.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					category.amount = new BigDecimal(e.value);
					redraw();
					mainview.updateExpenseCategory(category);
				}});
			sliders.put(category, slider);
		}
		
		addnewcategory = new DivRepButton(this, "Add New Bucket");
		addnewcategory.setStyle(DivRepButton.Style.ALINK);
		addnewcategory.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				mainview.category_dialog.open(null);
			}});
		
	}

	protected void onEvent(DivRepEvent e) {
		if(e.action.equals("remove")) {
			//remove
 			for(Category category : sliders.keySet()) {
				if(category.toString().equals(e.value)) {
					mainview.removeCategory(category);
		 			return;
				}
 			}
		} else {
			//edit
 			for(Category category : sliders.keySet()) {
				if(category.toString().equals(e.value)) {
					mainview.category_dialog.open(category);
		 			return;
				}
 			}
		}
	}

	public void render(PrintWriter out) {

		out.write("<div class=\"budgetting round8\" id=\""+getNodeID()+"\">");
		
		BigDecimal total_free_income = mainview.getTotalIncome();
		total_free_income = total_free_income.subtract(mainview.getTotalIncomeDeduction());
		BigDecimal total_unbudgetted = total_free_income.subtract(mainview.getTotalBudgetted());
		
		NumberFormat nf = NumberFormat.getCurrencyInstance();

		Long max = total_free_income.longValue();
		if(max > 0) {
			out.write("<table width=\"100%\">");
			out.write("<tr class=\"header\"><td colspan=2><h2>Budgeting</h2></td><th style=\"vertical-align: bottom\" class=\"note\">"+nf.format(0)+"</th><th class=\"note\" style=\"vertical-align: bottom; text-align: right;\">"+nf.format(total_free_income)+"</th><th style=\"text-align: right;\"></th><th width=\"10px\"></th></tr>");

			for(Category category : sliders.keySet()) {
	
				DivRepSlider slider = sliders.get(category);
				slider.setMax(max);
				slider.setValue(category.amount.longValue());
				slider.setColor(category.color);
		
				out.write("<tr class=\"category\" onclick=\"divrep('"+getNodeID()+"', event, '"+category.toString()+"')\">");
			
				out.write("<td width=\"20px\"></td>");
				
				out.write("<th width=\"270px\">"+category.name+"</th>");
				
				
				out.write("<td colspan=\"2\">");
				slider.render(out);
				out.write("</td>");
				
				out.write("<td style=\"text-align: right;\" width=\"70px\">");
				out.write(nf.format(category.amount));
				out.write("</td>");
		
				out.write("<td width=\"20px\">");
				out.write("<img onclick=\"divrep('"+getNodeID()+"', event, '"+category.toString()+"', 'remove');\" class=\"remove_button\" src=\"css/images/delete.png\"/>");
				out.write("</td>");			
			
				out.write("</tr>");
			}
			out.write("<tr class=\"header\"><th></th>");
			
			out.write("<td class=\"newitem\">");
			addnewcategory.render(out);
			out.write("</td>");
			
			String negative = "";
			if(total_unbudgetted.compareTo(BigDecimal.ZERO) < 0) {
				negative = "negative";
			}
			out.write("<th class=\"note\" style=\"text-align: right;\"></th><th style=\"text-align: right;\">Total Unbudgeted</th><th width=\"90px\" style=\"text-align: right;\" class=\""+negative+"\">"+nf.format(total_unbudgetted)+"</th><th></th></tr>");
		
			out.write("</table>");
			
			if(total_unbudgetted.compareTo(BigDecimal.ZERO) < 0) {
				out.write("<p class=\"divrep_elementerror\">Total budgeted is more than the total income. Please reduce the amount of budgets.</p>");
			}	
		}
			
		out.write("</div>");
	
	}

}
