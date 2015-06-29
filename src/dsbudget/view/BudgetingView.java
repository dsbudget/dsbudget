package dsbudget.view;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepSlider;

import dsbudget.Main;
import dsbudget.i18n.Labels;
import dsbudget.model.Category;

public class BudgetingView extends DivRep {
	MainView mainview;

	//LinkedHashMap<Category, DivRepSlider> sliders = new LinkedHashMap<Category, DivRepSlider>();
	DivRepButton addnewcategory;
	DivRepButton toggler;
	
	public BudgetingView(final MainView parent) {
		super(parent);
		mainview = parent;
	
		addnewcategory = new DivRepButton(this, Labels.getString(BUV_LABEL_ADD_BUCKET));
		addnewcategory.setStyle(DivRepButton.Style.ALINK);
		addnewcategory.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				mainview.category_dialog.open(null);
			}});
		
		toggler = new DivRepButton(this, "");
		toggler.setStyle(DivRepButton.Style.IMAGE);
		setTogglerIcon();
		toggler.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				mainview.page.hide_budget = !mainview.page.hide_budget;
				setTogglerIcon();
				redraw();
				mainview.save();
			}
		});
	}
	
	protected void setTogglerIcon()
	{
		if(mainview.page.hide_budget) {
			toggler.setTitle("css/images/expand.gif");
			toggler.setToolTip("Expand Budget View");
		} else {
			toggler.setTitle("css/images/collapse.gif");	
			toggler.setToolTip("Collapge Budget View");
		}
	}

	protected void onEvent(DivRepEvent e) {
		if(e.action.equals("remove")) {
			//remove
			/*
 			for(Category category : mainview.getCategories()) {
				if(category.toString().equals(e.value)) {
					mainview.removeCategory(category);
					mainview.save();
		 			return;
				}
 			}
 			*/
 			for(Category category : mainview.getCategories()) {
				if(category.toString().equals(e.value)) {
					mainview.removecategorydialog.open(category);
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
			mainview.save();
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
		total_free_income = total_free_income.subtract(mainview.getTotalIncomeDeduction());
		final AmountView total_unbudgetted_view = new AmountView(this, mainview.getTotalUnBudgetted());
		
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		NumberFormat pnf = NumberFormat.getPercentInstance();

		Long max = total_free_income.longValue();
		if(max > 0) {
			out.write("<table width=\"100%\">");
			out.write("<tr class=\"header\"><td width=\"310px\">");
			out.write("<h2>");
			out.write(Labels.getHtmlEscapedString(BUV_LABEL_HEADER));
			out.write("</h2></td>");
			
			out.write("<th style=\"vertical-align: bottom\" class=\"note\">");
			if(!mainview.page.hide_budget) {
				out.write(StringEscapeUtils.escapeHtml(nf.format(0)));
			}
			out.write("</th>");
			out.write("<th class=\"note\" style=\"vertical-align: bottom; text-align: right;\">");
			if(!mainview.page.hide_budget) {
				out.write(StringEscapeUtils.escapeHtml(nf.format(total_free_income)));
			}
			out.write("</th>");
			out.write("<th width=\"110px\" style=\"text-align: right;\"></th><th width=\"20px\">");
			toggler.render(out);
			out.write("</th></tr>");
			out.write("</table>");
			
			if(!mainview.page.hide_budget) {
				out.write("<ul id=\"budgetting_list\">");
				for(final Category category : mainview.getCategories()) {
					DivRepSlider slider = new DivRepSlider(this);
					slider.addEventListener(new DivRepEventListener() {
						public void handleEvent(DivRepEvent e) {
							if(e.action.equals("slidechange")) {
								category.setAmount(new BigDecimal(e.value));
								redraw();
								mainview.updateExpenseCategory(category);
								mainview.save();
							}
						}
					});
					
					slider.setListenSlideEvents(true);
					slider.setMax(max);
					slider.setValue(category.getAmount().longValue());
					slider.setColor(category.color);
					if(Main.conf.getProperty("disable_budget_slider", "false").equals("true")) {
						slider.setDisabled(true);
					}
					
					out.write("<li id=\"cat_"+category.getID()+"\" >");
	
					out.write("<table width=\"100%\">");
					out.write("<tr class=\"category\" onclick=\"divrep('"+getNodeID()+"', event, '"+category.toString()+"')\">");			
					out.write("<td width=\"20px\"><span class=\"sort_button ui-icon ui-icon-arrowthick-2-n-s\"></span></td>");		
					out.write("<th width=\"255px\">"+StringEscapeUtils.escapeHtml(category.name)+"</th>");
					
					//add new expense button
					out.write("<th width=\"15px\">");
					//Currency c = Currency.getInstance(Locale.getDefault());
					DivRepButton addnewexpense = new DivRepButton(this, "<span class=\"quick_expense_button ui-icon ui-icon-pencil\"></span>");
					addnewexpense.setStyle(DivRepButton.Style.HTML);
					addnewexpense.addEventListener(new DivRepEventListener() {
						public void handleEvent(DivRepEvent e) {
							mainview.expense_dialog.open(category, null);
						}
					});
					addnewexpense.render(out);
					out.write("</th>");				
					
					out.write("<td>");
					slider.render(out);
					out.write("</td>");
					
					out.write("<th style=\"text-align: right;\" width=\"110px\">");
					if(category.isPercentage()) {
						out.write("<span class=\"note\">(");
						out.write(StringEscapeUtils.escapeHtml((pnf.format(category.getPercentage()))));
						out.write(")</span> ");
					}
					final AmountView av = new AmountView(this, category.getAmount());
					av.render(out);
					slider.addEventListener(new DivRepEventListener(){
						public void handleEvent(DivRepEvent e) {
							if(e.action.equals("slide")) {
								category.setAmount(new BigDecimal(e.value));
	
								av.setValue(category.getAmount());
								av.redraw();
								
								total_unbudgetted_view.setValue(mainview.getTotalUnBudgetted());
								total_unbudgetted_view.redraw();
							}
						}});
					out.write("</th>");
			
					out.write("<td width=\"20px\">");
					out.write("<img onclick=\"divrep('"+getNodeID()+"', event, '"+category.toString()+"', 'remove');\" class=\"remove_button\" alt=\"remove\" src=\"css/images/delete.png\"/>");
					out.write("</td>");			
	
					out.write("</tr>");
					out.write("</table>");
					
					out.write("</li>");
				}
				out.write("</ul>");
			}
			
			out.write("<table width=\"100%\">");
			out.write("<tr class=\"header\"><th width=\"20px\"></th>");
			
			out.write("<td class=\"newitem\">");
			if(!mainview.page.hide_budget) {
				addnewcategory.render(out);
			}
			out.write("</td>");

			out.write("<th style=\"text-align: right;\">");
			out.write(Labels.getHtmlEscapedString(BUV_LABEL_TOTAL_UNBUDGETED));
			out.write("</th><th width=\"110px\" style=\"text-align: right;\">");
			total_unbudgetted_view.render(out);
			out.write("</th><th width=\"20px\"></th></tr>");
		
			out.write("</table>");
			
			out.write("<script type=\"text/javascript\">");
			out.write("$('#budgetting_list').sortable({tolerance: 'pointer', handle: 'span', containment: 'parent', stop: function(event, ui) {divrep('"+getNodeID()+"', event, ui.item.attr('id')+\"&\"+ui.item.prev().attr('id'));}, axis: 'y'}).disableSelection();");
			out.write("</script>");
			
			if(mainview.getTotalUnBudgetted().compareTo(BigDecimal.ZERO) < 0) {
				out.write("<p class=\"divrep_elementerror\">");
				out.write(Labels.getHtmlEscapedString(BUV_LABEL_EXCESSIVE_TOTAL_BUDGET));
				out.write("</p>");
			}
		} else {
			out.write("<h2>");
			out.write(Labels.getHtmlEscapedString(BUV_LABEL_HEADER));
			out.write("</h2>");
			out.write("<p class=\"divrep_elementerror\">");
			out.write(Labels.getHtmlEscapedString(BUV_LABEL_PLEASE_ADD_INCOME));
			out.write("</p>");
		}
			
		out.write("</div>");
	
	}
	public static final String BUV_LABEL_ADD_BUCKET = "BudgetingView.LABEL_ADD_BUCKET";
	public static final String BUV_LABEL_HEADER = "BudgetingView.LABEL_HEADER";
	public static final String BUV_LABEL_TOTAL_UNBUDGETED = "BudgetingView.LABEL_TOTAL_UNBUDGETED";
	public static final String BUV_LABEL_EXCESSIVE_TOTAL_BUDGET = "BudgetingView.LABEL_EXCESSIVE_TOTAL_BUDGET";
	public static final String BUV_LABEL_PLEASE_ADD_INCOME = "BudgetingView.LABEL_PLEASE_ADD_INCOME";

}
