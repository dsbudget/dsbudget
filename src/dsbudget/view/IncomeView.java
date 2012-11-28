package dsbudget.view;

import java.text.NumberFormat;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepButton.Style;

import dsbudget.i18n.Labels;
import dsbudget.model.Category;
import dsbudget.model.Deduction;
import dsbudget.model.Expense;
import dsbudget.model.Income;
import dsbudget.model.Page;

public class IncomeView extends DivRep {
	MainView mainview;

	DivRepButton addnewincome;
	DivRepButton toggler;
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	NumberFormat pnf = NumberFormat.getPercentInstance();
	
	public IncomeView(final MainView parent) {
		super(parent);
		mainview = parent;
		
		addnewincome = new DivRepButton(this, Labels.getString(INV_LABEL_ADD_NEW_INCOME));
		addnewincome.setStyle(DivRepButton.Style.ALINK);
		addnewincome.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				mainview.income_dialog.open(null);
			}});
		
		toggler = new DivRepButton(this, "");
		toggler.setStyle(DivRepButton.Style.IMAGE);
		setTogglerIcon();
		toggler.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				mainview.page.hide_income = !mainview.page.hide_income;
				setTogglerIcon();
				redraw();
				mainview.save();
			}
		});
	}
	
	protected void setTogglerIcon()
	{
		if(mainview.page.hide_income) {
			toggler.setTitle("css/images/expand.gif");
			toggler.setToolTip("Expand Income View");
		} else {
			toggler.setTitle("css/images/collapse.gif");	
			toggler.setToolTip("Collapse Income View");
		}
	}

	protected void onEvent(DivRepEvent e) {
		if(e.action.equals("remove")) {
			//remove
 			for(Income income : mainview.getIncomes()) {
				if(income.toString().equals(e.value)) {
					mainview.removeincomedialog.open(income);
		 			return;
				}
 			}
		} else if(e.action.equals("deduction_edit")) {
 			for(Income income : mainview.getIncomes()) {
	 			for(Deduction deduction : income.deductions) {
					if(deduction.toString().equals(e.value)) {
						mainview.deduction_dialog.open(income, deduction);
						return;
					}
	 			}
 			}
		} else if(e.action.equals("deduction_remove")) {
 			for(Income income : mainview.getIncomes()) {
	 			for(Deduction deduction : income.deductions) {
					if(deduction.toString().equals(e.value)) {
						income.deductions.remove(deduction);
						mainview.updateIncomeView();
						mainview.save();
						return;
					}
	 			}
 			}
		} else if(e.action.equals("sortstop")) {
			String [] tokens = e.value.split("&");
			Integer target_id = Integer.parseInt(tokens[0].split("_")[1]);
			
			Integer putafter_id = null;
			if(tokens[1].split("_").length == 2) {
				putafter_id = Integer.parseInt(tokens[1].split("_")[1]);
			}
			ArrayList<Income> incomes = mainview.getIncomes();
			ArrayList<Income> newlist = new ArrayList<Income>();
			
			//find target cat
			Income target = null;
			for(Income inc : incomes) {
				if(inc.getID().equals(target_id)) {
					target = inc;
					break;
				} 
			}
			//fint putafter_id = null
			Income putafter = null;
			for(Income inc : incomes) {
				if(inc.getID().equals(putafter_id)) {
					putafter = inc;
					break;
				} 
			}		
			//reorder
			if(putafter == null) {
				//put it at the beginning
				newlist.add(target);
			}
			for(Income inc : incomes) {
				if(inc != target) {
					newlist.add(inc);
				}
				if(inc == putafter) {
					newlist.add(target);
				}
			}
			
			mainview.changeIncomeOrder(newlist);
			mainview.save();
		} else {
			//edit
 			for(Income income : mainview.getIncomes()) {
				if(income.toString().equals(e.value)) {
					mainview.income_dialog.open(income);
					return;
				}
 			}
		}
	}
	public void render(PrintWriter out) {
		out.write("<div class=\"incomeview round8\" id=\""+getNodeID()+"\">");
		
		BigDecimal nettotal = new BigDecimal(0);
		
		out.write("<table width=\"100%\">");
		
		out.write("<tr>");
		out.write("<td>");
		out.write("<h2>");
		out.write(Labels.getHtmlEscapedString(INV_LABEL_HEADER));
		out.write("</h2>");
		out.write("</td>");
		if(!mainview.page.hide_income) {
			out.write("<th style=\"vertical-align: bottom; text-align: right\">");
			out.write(Labels.getHtmlEscapedString(INV_LABEL_AMOUNT));
			out.write("</th>");
			out.write("<th width=\"110px\" style=\"vertical-align: bottom; text-align: right\">");
			out.write(Labels.getHtmlEscapedString(INV_LABEL_DEDUCTIONS));
			out.write("</th>");
			out.write("<th width=\"110px\" style=\"vertical-align: bottom; text-align: right\">");
			out.write(Labels.getHtmlEscapedString(INV_LABEL_NET_INCOME));
			out.write("</th>");
		} else {
			out.write("<th colspan=\"3\"></th>");
		}
		out.write("<td width=\"20px\">");
		toggler.render(out);
		out.write("</td>");
		out.write("</tr>");
		out.write("</table>");
		
		out.write("<ul id=\"income_list\">");
		
		for(final Income income : mainview.getIncomes()) {	
			out.write("<li id=\"income_"+income.getID()+"\">");
			
			out.write("<table width=\"100%\">");
			
			DivRepButton addnewdeduction = new DivRepButton(this, Labels.getString(INV_LABEL_ADD_NEW_DEDUCTION));
			addnewdeduction.setStyle(DivRepButton.Style.ALINK);
			addnewdeduction.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					mainview.deduction_dialog.open(income, null);
				}});
			
			BigDecimal amount = income.getAmount();
			BigDecimal total_deduction = income.getTotalDeduction();
			BigDecimal total = amount.subtract(total_deduction);
			nettotal = nettotal.add(total);
			String name = income.getName();
			
			if(!mainview.page.hide_income) {
				//income
				out.write("<tr class=\"income\" onclick=\"divrep('"+getNodeID()+"', event, '"+income.toString()+"')\">");
				out.write("<th width=\"20px\"><span class=\"sort_button ui-icon ui-icon-arrowthick-2-n-s\"></span></th>");
	
				out.write("<th");
				if(income.balance_from != null) {
					out.write(" class=\"note\"");
				}
				out.write(">"+StringEscapeUtils.escapeHtml(name)+"</th>");
				
				if(income.deductions.size() > 0) {
					out.write("<td width=\"140px\" class=\"note\">");
					DivRepButton showhidedeductionbutton = new DivRepButton(this, Labels.getString(INV_LABEL_SHOW_DEDUCTIONS));
					if(income.show_deductions) {
						showhidedeductionbutton.setTitle(Labels.getString(INV_LABEL_HIDE_DEDUCTIONS));
					}
					showhidedeductionbutton.setStyle(Style.ALINK);
					showhidedeductionbutton.addEventListener(new DivRepEventListener() {
						public void handleEvent(DivRepEvent arg0) {
							redraw();
							income.show_deductions = !income.show_deductions;
							mainview.save();
						}});
					showhidedeductionbutton.render(out);
					out.write("</td>");
				} else {
					out.write("<td width=\"140px\" class=\"newitem\">");
					addnewdeduction.render(out);
					out.write("</td>");
				}
				
				if(income.deductions.size() > 0) {
					out.write("<th width=\"110px\" style=\"text-align: right;\">");
					AmountView av_amount = new AmountView(this, amount);
					av_amount.render(out);
					out.write("</th>");
					out.write("<th width=\"110px\" style=\"text-align: right;\">"+StringEscapeUtils.escapeHtml(nf.format(income.getTotalDeduction()))+"</th>");
				} else {
					out.write("<th width=\"110px\"></th>");
					out.write("<th width=\"110px\"></th>");
				}

				out.write("<th width=\"110px\" style=\"text-align: right;\">");
				AmountView av_total = new AmountView(this, total);
				av_total.render(out);
				out.write("</th>");
				
				out.write("<td width=\"20px\">");
				out.write("<img onclick=\"divrep('"+getNodeID()+"', event, '"+income.toString()+"', 'remove');\" class=\"remove_button\" alt=\"remove\" src=\"css/images/delete.png\"/>");
				out.write("</td>"); //TODO - remove icon
				out.write("</tr>");
				
				//deduction
				if(income.show_deductions) {
					for(Deduction deduction : income.deductions) {
						out.write("<tr class=\"deduction\" onclick=\"divrep('"+getNodeID()+"', event, '"+deduction.toString()+"', 'deduction_edit')\">");
						out.write("<th>&nbsp;</th>");
						out.write("<td>"+StringEscapeUtils.escapeHtml(deduction.description)+"</td>");
						out.write("<td></td>");
						out.write("<td></td>");
	
						out.write("<td width=\"110px\" style=\"text-align: right;\">");
						if(deduction.isPercentage()) {
							out.write("<span class=\"note\">(");
							out.write(StringEscapeUtils.escapeHtml((pnf.format(deduction.getPercentage()))));
							out.write(")</span> ");
						}
						AmountView av_deduction = new AmountView(this, deduction.getAmount(amount));
						av_deduction.render(out);
						out.write("</td>");
						
						out.write("<td></td>");
						out.write("<td>");
						out.write("<img onclick=\"divrep('"+getNodeID()+"', event, '"+deduction.toString()+"', 'deduction_remove');\" class=\"remove_button\" alt=\"remove\" src=\"css/images/delete.png\"/>");
						out.write("</td>");
						out.write("</tr>");
					}
				}
				
				//total deduction
				if(income.deductions.size() > 0 && income.show_deductions == true) {
					out.write("<tr class=\"info\">");
					out.write("<th>&nbsp;</th>");
					out.write("<td class=\"newitem\">");
					addnewdeduction.render(out);
					out.write("</td>");
					out.write("<td></td>");
					out.write("<th></th>");
					out.write("<th></th>");
					out.write("<td></td>");
					out.write("<td></td>");
					out.write("</tr>");
				}
			}	
			
			out.write("</table>");
			
			out.write("</li>");
		}
		
		out.write("</ul>");
		out.write("<table width=\"100%\">");
		out.write("<tr class=\"header\">");
		out.write("<th width=\"20px\"></th>");
		out.write("<td class=\"newitem\">");
		if(!mainview.page.hide_income) {
			addnewincome.render(out);
		}
		out.write("</td>");
		out.write("<td></td>");
		if(mainview.getIncomes().size() > 1) {
			out.write("<th colspan=\"2\" style=\"text-align: right;\">");
			out.write(Labels.getHtmlEscapedString(INV_LABEL_TOTAL_NET_INCOME));
			out.write("</th><th width=\"110px\" style=\"text-align: right;\">"+StringEscapeUtils.escapeHtml((nf.format(nettotal)))+"</th>");
		} else {
			out.write("<th></th><th></th><th></th>");
			
		}
		out.write("<th width=\"20px\">&nbsp;</th></tr>");
		out.write("</table>");
		
		out.write("<script type=\"text/javascript\">");
		out.write("$('#income_list').sortable({tolerance: 'pointer', handle: 'span', containment: 'parent', stop: function(event, ui) {divrep('"+getNodeID()+"', event, ui.item.attr('id')+\"&\"+ui.item.prev().attr('id'));}, axis: 'y'}).disableSelection();");
		out.write("</script>");
		
		out.write("</div>");
	}

	public static final String INV_LABEL_HEADER = "IncomeView.LABEL_HEADER";
	public static final String INV_LABEL_ADD_NEW_INCOME = "IncomeView.LABEL_ADD_NEW_INCOME";
	public static final String INV_LABEL_ADD_NEW_DEDUCTION = "IncomeView.LABEL_ADD_NEW_DEDUCTION";
	public static final String INV_LABEL_AMOUNT = "IncomeView.LABEL_AMOUNT";
	public static final String INV_LABEL_DEDUCTIONS = "IncomeView.LABEL_DEDUCTIONS";
	public static final String INV_LABEL_NET_INCOME = "IncomeView.LABEL_NET_INCOME";
	public static final String INV_LABEL_SHOW_DEDUCTIONS = "IncomeView.LABEL_SHOW_DEDUCTIONS";
	public static final String INV_LABEL_HIDE_DEDUCTIONS = "IncomeView.LABEL_HIDE_DEDUCTIONS";
	public static final String INV_LABEL_TOTAL_NET_INCOME = "IncomeView.LABEL_TOTAL_NET_INCOME";
	public static final String INV_LABEL_BALANCE_FROM = "IncomeView.LABEL_BALANCE_FROM";

}

