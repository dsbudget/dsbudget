package dsbudget.view;

import java.awt.Color;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepSlider;

import dsbudget.i18n.Labels;
import dsbudget.model.Category;

class BalanceView extends DivRep 
{
	MainView mainview;
	
	DivRepButton toggler;
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	DateFormat df = DateFormat.getDateInstance();
	
	public BalanceView(final MainView parent) {
		super(parent);
		mainview = parent;
		
		toggler = new DivRepButton(this, "");
		toggler.setStyle(DivRepButton.Style.IMAGE);
		setTogglerIcon();
		toggler.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				mainview.page.hide_balance = !mainview.page.hide_balance;
				setTogglerIcon();
				redraw();
				mainview.save();
			}
		});
	}
	
	protected void setTogglerIcon()
	{
		if(mainview.page.hide_balance) {
			toggler.setTitle("css/images/expand.gif");
			toggler.setToolTip("Expand Balance View");
		} else {
			toggler.setTitle("css/images/collapse.gif");	
			toggler.setToolTip("Collapse Balance View");
		}
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(PrintWriter out) {
		out.write("<div class=\"balanceview round8\" id=\""+getNodeID()+"\">");
		
		out.write("<table width=\"100%\"><tr><td width=\"300px\">");
		out.write("<h2>");
		out.write(Labels.getHtmlEscapedString(BAV_LABEL_HEADER));
		out.write("</h2>");
		out.write("</td>");
		
		out.write("<th style=\"vertical-align: bottom; text-align: right\" width=\"90px\">"+Labels.getHtmlEscapedString("BalanceView.LABEL_BUDGET")+"</th>");
		out.write("<th style=\"vertical-align: bottom; text-align: right\" width=\"90px\">"+Labels.getHtmlEscapedString("BalanceView.LABEL_SPENT")+"</th>");
		out.write("<th style=\"vertical-align: bottom; text-align: right\" width=\"90px\">"+Labels.getHtmlEscapedString("BalanceView.LABEL_REMAINING")+"</th>");
		out.write("<th style=\"vertical-align: bottom; text-align: left\" class=\"note\">0%</th>");
		out.write("<th style=\"vertical-align: bottom; text-align: right\" class=\"note\">100%</th>");
		
		out.write("<th width=\"20px\">");
		toggler.render(out);
		out.write("</th>");
		out.write("</tr></table>");
		
		if(!mainview.page.hide_balance) {
			out.write("<ul id=\"balanceview_list\">");
			for(final Category category : mainview.getCategories()) {
				
				out.write("<li>");
				out.write("<table width=\"100%\">");
				
				Color orig = category.color;
				int r = (255 + 255 + orig.getRed())/3;
				int g = (255 + 255 + orig.getGreen())/3;
				int b = (255 + 255 + orig.getBlue())/3;
				Color header_color = new Color(r,g,b);
				out.write("<tr style=\"background-color: #"+String.format("%06x", (header_color.getRGB() & 0x00ffffff) )+";\" class=\"category\" onclick=\"divrep('"+getNodeID()+"', event, '"+category.toString()+"')\">");			
				
				//out.write("<tr class=\"category\" onclick=\"divrep('"+getNodeID()+"', event, '"+category.toString()+"')\">");			

				out.write("<td width=\"20px\">&nbsp;</td>");	
				out.write("<th width=\"270px\">"+StringEscapeUtils.escapeHtml(category.name)+"</th>");

				//budget
				out.write("<td style=\"text-align: right;\" width=\"90px\">");
				AmountView av = new AmountView(this, category.amount);
				av.render(out);
				out.write("</td>");
				
				//spent
				out.write("<td style=\"text-align: right;\" width=\"90px\">");
				av = new AmountView(this, category.getTotalExpense());
				av.render(out);
				out.write("</td>");
				
				//remaining
				out.write("<td style=\"text-align: right;\" width=\"90px\">");
				av = new AmountView(this, category.amount.subtract(category.getTotalExpense()));
				av.render(out);
				out.write("</td>");
				
				//graph
				out.write("<td>");
				int amount = category.amount.intValue();
				int spent = category.getTotalExpense().intValue()*100;
				int percentage = 0;
				if(amount != 0) {
					percentage = spent / amount;
				}
				if(percentage > 100) {
					percentage = 100;
				}
				DivRepProgressbar pbar = new DivRepProgressbar(this);
				pbar.setValue(100-percentage);
				//pbar.setColor(category.color);
				pbar.render(out);
				out.write("</td>");	
				
				out.write("<th width=\"20px\"></th>");
				out.write("</tr>");
				out.write("</table>");
				
				out.write("</li>");
			}
			
			//unbudgeted income
			BigDecimal unbudgetted = mainview.getTotalUnBudgetted();
			if(unbudgetted.compareTo(BigDecimal.ZERO) != 0) {
				out.write("<li>");
				out.write("<table width=\"100%\">");
				out.write("<tr>");
				out.write("<td width=\"20px\">&nbsp;</td>");	
				out.write("<th style=\"color: #666;\" width=\"270px\">"+Labels.getHtmlEscapedString("BalanceView.LABEL_UNBUDGETED_INCOME")+"</th>");
				out.write("<td style=\"text-align: right;\" width=\"90px\"></td>"); //budget
				out.write("<td style=\"text-align: right;\" width=\"90px\"></td>"); //spent
				
				//remaining
				out.write("<td style=\"text-align: right;\" width=\"90px\">");
				AmountView av = new AmountView(this, unbudgetted);
				av.render(out);
				out.write("</td>");
			
				out.write("<td></td>");	//graph
				
				out.write("<th width=\"20px\"></th>");
				
				out.write("</tr>");
				out.write("</table>");
				out.write("</li>");		
			}
			
			out.write("</ul>");
		}
		
		out.write("<table class=\"balancetable\" width=\"100%\">");
		
		out.write("<tr class=\"balance_header\">");
		out.write("<th width=\"20px\"></th>");
		out.write("<th width=\"270px\">"+Labels.getHtmlEscapedString("BalanceView.LABEL_TOTAL")+"</th>");
		out.write("<th width=\"90px\" style=\"text-align: right;\">");
		BigDecimal total_budgetted = mainview.getTotalBudgetted();
		AmountView av = new AmountView(this, total_budgetted);
		av.render(out);
		out.write("</th>");
		out.write("<th width=\"90px\" style=\"text-align: right;\">");
		BigDecimal total_spent = mainview.getTotalExpense();
		av = new AmountView(this, total_spent);
		av.render(out);
		out.write("</th>");
		out.write("<th width=\"90px\" style=\"text-align: right;\">");
		av = new AmountView(this, mainview.getBalance());
		av.render(out);
		out.write("</th>");
		
		//graph
		out.write("<td>");
		int amount = total_budgetted.intValue();
		int spent = total_spent.intValue()*100;
		int percentage = 0;
		if(amount != 0) {
			percentage = spent / amount;
		}
		if(percentage > 100) {
			percentage = 100;
		}
		DivRepProgressbar pbar = new DivRepProgressbar(this);
		pbar.setValue(100-percentage);
		//pbar.setColor(category.color);
		pbar.render(out);
		out.write("</td>");	
		
		out.write("<th width=\"20px\">");
		out.write("</th>");
		
		out.write("</tr>");
	
		
		out.write("</table>");
		
		out.write("</div>");
	}
	/*
	private void renderAmountTD(PrintWriter out, BigDecimal amount) {
		String negative = "";
		if(amount.compareTo(BigDecimal.ZERO) < 0) {
			negative = "negative";
		}
		out.write("<td style=\"text-align: right;\" class=\""+negative+"\">" + nf.format(amount) + "</td>");
	}
	*/
	
	public static final String BAV_LABEL_HEADER = "BalanceView.LABEL_HEADER";;

}