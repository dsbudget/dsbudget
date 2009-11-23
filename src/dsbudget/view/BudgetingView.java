package dsbudget.view;

import java.text.NumberFormat;
import java.text.ParseException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.lang.StringEscapeUtils;

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

	//LinkedHashMap<Category, DivRepSlider> sliders = new LinkedHashMap<Category, DivRepSlider>();
	DivRepButton addnewcategory;
	
	public BudgetingView(final MainView parent) {
		super(parent);
		mainview = parent;
/*
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
*/		
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
 			for(Category category : mainview.getCategories()) {
				if(category.toString().equals(e.value)) {
					mainview.removeCategory(category);
		 			return;
				}
 			}
		} else if(e.action.equals("sortstop")) {
			String [] tokens = e.value.split("&");
			Integer target_id = Integer.parseInt(tokens[0].split("_")[1]);
			
			Integer putafter_id = null;
			if(tokens[1].split("_").length == 2) {
				putafter_id = Integer.parseInt(tokens[1].split("_")[1]);
			}
			ArrayList<Category> categories = mainview.getCategories();
			ArrayList<Category> newlist = new ArrayList<Category>();
			
			//find target cat
			Category target = null;
			for(Category cat : categories) {
				if(cat.getID().equals(target_id)) {
					target = cat;
					break;
				} 
			}
			//fint putafter_id = null
			Category putafter = null;
			for(Category cat : categories) {
				if(cat.getID().equals(putafter_id)) {
					putafter = cat;
					break;
				} 
			}		
			//reorder
			if(putafter == null) {
				//put it at the beginning
				newlist.add(target);
			}
			for(Category cat : categories) {
				if(cat != target) {
					newlist.add(cat);
				}
				if(cat == putafter) {
					newlist.add(target);
				}
			}
			
			mainview.setCategories(newlist);
		} else {
			//edit
 			for(Category category : mainview.getCategories()) {
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
		//total_free_income = total_free_income.subtract(mainview.getTotalIncomeDeduction());
		//BigDecimal total_unbudgetted = total_free_income.subtract(mainview.getTotalBudgetted());
		final AmountView total_unbudgetted_view = new AmountView(this, mainview.getTotalUnBudgetted());
		
		NumberFormat nf = NumberFormat.getCurrencyInstance();

		Long max = total_free_income.longValue();
		if(max > 0) {
			out.write("<table width=\"100%\">");
			out.write("<tr class=\"header\"><td width=\"300px\"><h2>Budgeting</h2></td><th style=\"vertical-align: bottom\" class=\"note\">"+nf.format(0)+"</th><th class=\"note\" style=\"vertical-align: bottom; text-align: right;\">"+nf.format(total_free_income)+"</th><th width=\"90px\" style=\"text-align: right;\"></th><th width=\"20px\"></th></tr>");
			out.write("</table>");
			
			out.write("<style>");
			out.write("#budgetting_list { list-style-type: none; margin: 0; padding: 0; width: 100%; }");
			//out.write("#budgetting_list li { margin: 0px; padding: 0px;border: 0px;}");
			out.write("</style>");
			
			out.write("<ul id=\"budgetting_list\">");
			//for(Category category : sliders.keySet()) {
			for(final Category category : mainview.getCategories()) {
				DivRepSlider slider = new DivRepSlider(this);
				slider.addEventListener(new DivRepEventListener() {
					public void handleEvent(DivRepEvent e) {
						if(e.action.equals("slidechange")) {
							category.amount = new BigDecimal(e.value);
							redraw();
							mainview.updateExpenseCategory(category);
						}
					}});
				//sliders.put(category, slider);
				//DivRepSlider slider = sliders.get(category);
				
				slider.setListenSlideEvents(true);
				slider.setMax(max);
				slider.setValue(category.amount.longValue());
				slider.setColor(category.color);
				
				out.write("<li id=\"cat_"+category.getID()+"\" >");

				out.write("<table id=\"budgetting_table\" width=\"100%\">");
				out.write("<tr class=\"category\" onclick=\"divrep('"+getNodeID()+"', event, '"+category.toString()+"')\">");			
				out.write("<td width=\"20px\"><span class=\"sort_button ui-icon ui-icon-arrowthick-2-n-s\"></span></td>");				
				out.write("<th width=\"270px\">"+StringEscapeUtils.escapeHtml(category.name)+"</th>");
								
				out.write("<td colspan=\"2\">");
				slider.render(out);
				out.write("</td>");
				
				out.write("<th style=\"text-align: right;\" width=\"90px\">");
				final AmountView av = new AmountView(this, category.amount);
				av.render(out);
				slider.addEventListener(new DivRepEventListener(){
					public void handleEvent(DivRepEvent e) {
						if(e.action.equals("slide")) {
							category.amount = new BigDecimal(e.value);

							av.setValue(category.amount);
							av.redraw();
							
							total_unbudgetted_view.setValue(mainview.getTotalUnBudgetted());
							total_unbudgetted_view.redraw();
						}
					}});
				out.write("</th>");
		
				out.write("<td width=\"20px\">");
				out.write("<img onclick=\"divrep('"+getNodeID()+"', event, '"+category.toString()+"', 'remove');\" class=\"remove_button\" src=\"css/images/delete.png\"/>");
				out.write("</td>");			

				out.write("</tr>");
				out.write("</table>");
				
				out.write("</li>");
			}
			out.write("</ul>");
			
			out.write("<table width=\"100%\">");
			out.write("<tr class=\"header\"><th width=\"20px\"></th>");
			
			out.write("<td class=\"newitem\">");
			addnewcategory.render(out);
			out.write("</td>");

			out.write("<th class=\"note\" style=\"text-align: right;\">Total Unbudgeted</th><th width=\"90px\" style=\"text-align: right;\" class=\"note\">");
			total_unbudgetted_view.render(out);
			out.write("</th><th width=\"20px\"></th></tr>");
		
			out.write("</table>");
			
			out.write("<script type=\"text/javascript\">");
			out.write("$('#budgetting_list').sortable({tolerance: 'pointer', handle: 'span', containment: 'parent', stop: function(event, ui) {divrep('"+getNodeID()+"', event, ui.item.attr('id')+\"&\"+ui.item.prev().attr('id'));}, axis: 'y'}).disableSelection();");
			out.write("</script>");
			
			if(mainview.getTotalUnBudgetted().compareTo(BigDecimal.ZERO) < 0) {
				out.write("<p class=\"divrep_elementerror\">Total budgeted is more than the total income. Please reduce the amount of budgets.</p>");
			}
		} else {
			out.write("<h2>Budgeting</h2>");
			out.write("<p class=\"divrep_elementerror\">Please add income.</p>");
		}
			
		out.write("</div>");
	
	}

}
