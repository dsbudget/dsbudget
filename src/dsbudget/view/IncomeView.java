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

import dsbudget.model.Category;
import dsbudget.model.Deduction;
import dsbudget.model.Expense;
import dsbudget.model.Income;
import dsbudget.model.Page;

public class IncomeView extends DivRep {
	MainView mainview;

	DivRepButton addnewincome;
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	
	public IncomeView(final MainView parent) {
		super(parent);
		mainview = parent;
		
		addnewincome = new DivRepButton(this, "Add New Income");
		addnewincome.setStyle(DivRepButton.Style.ALINK);
		addnewincome.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				mainview.income_dialog.open(null);
			}});
		
	}

	protected void onEvent(DivRepEvent e) {
		if(e.action.equals("remove")) {
			//remove
 			for(Income income : mainview.getIncomes()) {
				if(income.toString().equals(e.value)) {
					mainview.removeIncome(income);
					mainview.save();
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
		out.write("<td colspan=\"3\">");
		out.write("<h2>Income &amp; Deductions</h2>");
		out.write("</td>");
		out.write("<th style=\"vertical-align: bottom; text-align: right\">Amount</th>");
		out.write("<th width=\"90px\" style=\"vertical-align: bottom; text-align: right\">Deductions</th>");
		out.write("<th width=\"90px\" style=\"vertical-align: bottom; text-align: right\">Net Amount</th>");
		out.write("<td></td>");
		out.write("</td>");
		for(final Income income : mainview.getIncomes()) {	
			
			DivRepButton addnewdeduction = new DivRepButton(this, "Add New Deduction");
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
		
			//income
			out.write("<tr class=\"income\" onclick=\"divrep('"+getNodeID()+"', event, '"+income.toString()+"')\">");
			out.write("<th width=\"20px\"></th>");

			out.write("<th width=\"270px\">"+StringEscapeUtils.escapeHtml(name)+"</th>");
			
			if(income.deductions.size() > 0) {
				out.write("<td class=\"note\">");
				DivRepButton showhidedeductionbutton = new DivRepButton(this, "Show Deductions");
				if(income.show_deductions) {
					showhidedeductionbutton.setTitle("Hide Deductions");
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
				out.write("<td class=\"newitem\">");
				addnewdeduction.render(out);
				out.write("</td>");
			}
			String negative = "";
			
			if(income.deductions.size() > 0) {
				if(amount.compareTo(BigDecimal.ZERO) < 0) {
					negative = "negative";
				}
				out.write("<th style=\"text-align: right;\" class=\""+negative+"\">"+nf.format(amount)+"</th>");
				out.write("<th style=\"text-align: right;\">"+nf.format(income.getTotalDeduction())+"</th>");
			} else {
				out.write("<th></th>");
				out.write("<th></th>");
			}
			
			negative = "";
			if(total.compareTo(BigDecimal.ZERO) < 0) {
				negative = "negative";
			}
			out.write("<th style=\"text-align: right;\" class=\""+negative+"\">"+nf.format(total)+"</th>");
			
			out.write("<td width=\"20px\">");
			out.write("<img onclick=\"divrep('"+getNodeID()+"', event, '"+income.toString()+"', 'remove');\" class=\"remove_button\" src=\"css/images/delete.png\"/>");
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
					negative = "";
					if(deduction.amount.compareTo(BigDecimal.ZERO) < 0) {
						negative = "negative";
					}
					out.write("<td width=\"90px\" style=\"text-align: right;\" class=\""+negative+"\">"+nf.format(deduction.amount)+"</td>");
					out.write("<td></td>");
					out.write("<td>");
					out.write("<img onclick=\"divrep('"+getNodeID()+"', event, '"+deduction.toString()+"', 'deduction_remove');\" class=\"remove_button\" src=\"css/images/delete.png\"/>");
					out.write("</td>");
					out.write("</tr>");
				}
			}
			
			//total deduction
			if(income.deductions.size() > 0) {
				out.write("<tr class=\"info\">");
				out.write("<th>&nbsp;</th>");
				out.write("<td class=\"newitem\">");
				addnewdeduction.render(out);
				out.write("</td>");
				out.write("<td></td>");
				out.write("<th></th>");
				out.write("<th></th>");
				out.write("<td></td>");
			}
			out.write("<td></td>");
			out.write("</tr>");
			/*
			//total available income
			if(income.deductions.size() > 0) {
				out.write("<tr class=\"info\">");
				out.write("<th>&nbsp;</th>");
				out.write("<td></td>");
				out.write("<td></td>");
				out.write("<th class=\"note\" style=\"text-align: right;\">Available Income</th>");
				out.write("<th class=\"note\" style=\"text-align: right;\">"+nf.format(total)+"</th>");
				out.write("<td></td>");
				out.write("</tr>");
			}
			*/
		}		
		
		out.write("<tr class=\"header\">");
		out.write("<th></th>");
		out.write("<td class=\"newitem\">");
		addnewincome.render(out);
		out.write("</td>");
		out.write("<td></td>");
		if(mainview.getIncomes().size() > 1) {
			out.write("<th colspan=\"2\" style=\"text-align: right;\">Total Net Amount</th><th style=\"text-align: right;\">"+nf.format(nettotal)+"</th>");
		} else {
			out.write("<th></th><th></th><th></th>");
			
		}
		out.write("<th>&nbsp;</th></tr>");
		out.write("</table>");
		
		out.write("</div>");
	}

}

