package dsbudget.view;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;

class BalanceView extends DivRep 
{
	MainView mainview;
	
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	DateFormat df = DateFormat.getDateInstance();
	
	public BalanceView(final MainView parent) {
		super(parent);
		mainview = parent;
	}
	

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(PrintWriter out) {
		out.write("<div class=\"balanceview round8\" id=\""+getNodeID()+"\">");
		out.write("<h2>Overview</h2>");
		
		out.write("<table class=\"balancetable\" width=\"100%\">");
		
		out.write("<tr class=\"balance_header\">");
		out.write("<th width=\"20px\"></th>");
		out.write("<th style=\"text-align: left;\">Total Net Income</th>");
		out.write("<th style=\"text-align: left;\">Total Expense</th>");
		out.write("<th style=\"text-align: left;\">Total Balance</th>");
		out.write("<th width=\"20px\"></th>");
		out.write("</tr>");
		
		out.write("<tr>");
		out.write("<td></td>");
		renderAmountTD(out,  mainview.getTotalNetIncome());
		renderAmountTD(out,  mainview.getTotalExpense());
		renderAmountTD(out,  mainview.getBalance());
		out.write("<td></td>");
		out.write("</tr>");
		
		out.write("</table>");
		
		out.write("</div>");
	}
	
	private void renderAmountTD(PrintWriter out, BigDecimal amount) {
		String negative = "";
		if(amount.compareTo(BigDecimal.ZERO) < 0) {
			negative = "negative";
		}
		out.write("<td class=\""+negative+"\">" + nf.format(amount) + "</td>");
	}
}