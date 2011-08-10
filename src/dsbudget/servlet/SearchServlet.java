package dsbudget.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepPage;
import com.divrep.DivRepRoot;

import com.divrep.common.DivRepTextBox;

import dsbudget.i18n.Labels;
import dsbudget.model.Category;
import dsbudget.model.Deduction;
import dsbudget.model.Expense;
import dsbudget.model.Income;
import dsbudget.model.Page;

public class SearchServlet extends PageServletBase  {
	
	DivRepTextBox query;
	ResultArea resultarea;
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	
    public SearchServlet() {
        super();
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String export = request.getParameter("export");
		if(export != null) {
			PrintWriter out = response.getWriter();
			/*
			DivRepRoot root = DivRepRoot.getInstance(request.getSession());
			ResultArea resultarea = (ResultArea) root.findNode(export_divrep_id);
			*/
			
            response.setContentType("text/csv");
            String disposition = "attachment; fileName="+export+".csv";
            response.setHeader("Content-Disposition", disposition);

			if(export.equals("deduction")) {
				DeductionSearchResult.renderCSVHeader(out);
				for(SearchResult result : resultarea.deduction_results) {
					result.renderCSV(out);
				}
			} else if(export.equals("expense")) {
				
				ExpenseSearchResult.renderCSVHeader(out);
				for(SearchResult result : resultarea.expense_results) {
					result.renderCSV(out);
				}
			}
		} else {
			super.doGet(request, response);
		}
	}
    
    public class SearchEventListener extends DivRepEventListener {

		public void handleEvent(DivRepEvent e) {
			resultarea.clearResult();
			resultarea.redraw();
			if(e.value.length() < 3) return; //maybe I should have "search" button..
			
			doSearch(e.value.split(" "));
		}
    }
    
    public void doSearch(String[] terms) {
    	String primary_term = terms[0];
    	 	
    	//search on primary term
    	for(Page page : budget.pages) {
    		for(Income income : page.incomes) {    			
    			//search deduction
    			for(Deduction deduction : income.deductions) {
        			if(deduction.description.toLowerCase().contains(primary_term.toLowerCase())) {
        				resultarea.deduction_results.add(new DeductionSearchResult(income, deduction));
        			}
        			
    			}
    		}
    		
    		for(Category category : page.categories) {
    			//search expense
    			for(Expense expense : category.expenses) {
        			if(
        					expense.description.toLowerCase().contains(primary_term.toLowerCase()) ||
        					category.name.toLowerCase().contains(primary_term.toLowerCase()) ||
        					expense.where.toLowerCase().contains(primary_term.toLowerCase())
        			) {
        				resultarea.expense_results.add(new ExpenseSearchResult(category, expense));
        			}
    			}
    		}
    	}
    	
    	//filter by secondary terms
    	if(terms.length > 1) {
    		resultarea.deduction_results = applySecondarySearch(terms, resultarea.deduction_results);
    		resultarea.expense_results = applySecondarySearch(terms, resultarea.expense_results);
    	}
    }
    
    private ArrayList<SearchResult> applySecondarySearch(String [] terms, ArrayList<SearchResult> results) {
    	//exaimin each search results
    	ArrayList<SearchResult> real_results = new ArrayList<SearchResult>();
    	for(SearchResult result : results) {
    		Boolean first = true;
    		Boolean found = false;
	    	for(String term : terms) {
	    		//ignore the primary term
	    		if(first) {
	    			first = false;
	    			continue;
	    		}
    			if(result.getTerm().toLowerCase().contains(term.toLowerCase())) {
    				found = true;
    				break;
    			}
	    	}
	    	if(found) {
	    		real_results.add(result);
	    	}
    	}
    	
    	return real_results;
    }
    		
	protected void renderMain(PrintWriter out, HttpServletRequest request)
	{					
		out.write("<div id=\"main\">");
		
		out.write("<div class=\"pagename\">"+Labels.getHtmlEscapedString("Search.LABEL_PAGE_NAME")+"</div>");
		
		query.render(out);		
		out.write("<br>");
		resultarea.render(out);
		
		out.write("</div>"); //main

		//TODO - output dialog
	}
	
	class ResultArea extends DivRep {
		
		ArrayList<SearchResult> deduction_results = new ArrayList<SearchResult>();
		ArrayList<SearchResult> expense_results = new ArrayList<SearchResult>();
		
		public ResultArea(DivRep parent) {
			super(parent);
		}
		
		public void clearResult() {
			deduction_results.clear();
			expense_results.clear();
		}
				
		public void render(PrintWriter out) {
			out.write("<div id=\""+this.getNodeID()+"\">");
		
			if(deduction_results.size() > 0) {
			
				//deduction matches
				out.write("<div class=\"search_result round8\">");
				out.write("<span class=\"export\" style=\"padding-right: 40px;\"><a target=\"_blank\" href=\"search?export=deduction\">");
				out.write(Labels.getHtmlEscapedString("Search.LABEL_EXPORT"));
				out.write("</a></span>");
				out.write("<h2>Deduction</h2>");
				out.write("<table>");
				
				//columns heaeders
				out.write("<tr>");
				out.write("<th></th>");
				out.write("<th>"+Labels.getHtmlEscapedString("Search.LABEL_PAGE")+"</th>");
				out.write("<th>"+Labels.getHtmlEscapedString("Search.LABEL_INCOME")+"</th>");
				out.write("<th>"+Labels.getHtmlEscapedString("Search.LABEL_DEDUCTION")+"</th>");
				out.write("<th style=\"width: 110px; text-align: right;\">"+Labels.getHtmlEscapedString("Search.LABEL_AMOUNT")+"</th>");
				out.write("<th></th>");
				out.write("</tr>");		
				
				//rows
				BigDecimal total = new BigDecimal(0);
				int count = 0;
				for(SearchResult result : deduction_results) {
					total = total.add(result.getAmount());
					result.render(out);
					count++;
				}
				
				//render total
				out.write("<tr class=\"total\">");
				out.write("<th colspan=\"4\" style=\"text-align: right;\">"+Labels.getHtmlEscapedString("Search.LABEL_TOTAL")+" ("+count+" Items)</th>");
				out.write("<th style=\"text-align: right;\">"+StringEscapeUtils.escapeHtml(nf.format(total))+"</th>");
				out.write("<th></th>");
				out.write("</tr>");	
				
				out.write("</table>");
				out.write("<br>");
				out.write("</div>");
				
				out.write("<br>");
			}
				
			if(expense_results.size() > 0) {
				//expense matches
				out.write("<div class=\"search_result round8\">");
				out.write("<span class=\"export\" style=\"padding-right: 40px;\"><a target=\"_blank\" href=\"search?export=expense\">");
				out.write(Labels.getHtmlEscapedString("Search.LABEL_EXPORT"));
				out.write("</a></span>");
				out.write("<h2>Expenses</h2>");
				out.write("<table>");
				
				//column header	
				out.write("<tr>");
				out.write("<th></th>");
				out.write("<th>"+Labels.getHtmlEscapedString("Search.LABEL_PAGE")+"</th>");
				out.write("<th>"+Labels.getHtmlEscapedString("Search.LABEL_CATEGORY")+"</th>");
				out.write("<th>"+Labels.getHtmlEscapedString("Search.LABEL_WHERE")+"</th>");
				out.write("<th></th>");//description
				out.write("<th style=\"text-align: right;\"></th>");//date
				out.write("<th style=\"width: 110px; text-align: right;\">"+Labels.getHtmlEscapedString("Search.LABEL_AMOUNT")+"</th>");
				out.write("<th></th>");
				out.write("</tr>");		
				
				//rows
				BigDecimal total = new BigDecimal(0);
				int count = 0;
				for(SearchResult result : expense_results) {
					total = total.add(result.getAmount());
					result.render(out);
					count++;
				}
				
				//render total
				out.write("<tr class=\"total\">");
				out.write("<th colspan=\"6\" style=\"text-align: right;\">"+Labels.getHtmlEscapedString("Search.LABEL_TOTAL")+" ("+count+" Items)</th>");
				out.write("<th style=\"text-align: right;\">"+StringEscapeUtils.escapeHtml(nf.format(total))+"</th>");
				out.write("<th></th>");
				out.write("</tr>");	
				
				out.write("</table>");
				out.write("<br>");
				out.write("</div>");
				out.write("<br>");
			
			}
			out.write("</div>");
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	protected void decidePageToOpen(HttpServletRequest request)
	{
		current_page = Page.search_page;
	}

	@Override
	protected void initDivRepObjects(DivRepPage pageroot) {
		query = new DivRepTextBox(pageroot);
		query.setEventMode("onkeyup");
		query.addEventListener(new SearchEventListener());
		query.setSampleValue("Enter Search Keyword");

		resultarea = new ResultArea(pageroot);		
	}
}

interface SearchResult {
	public String getTerm();
	public void render(PrintWriter out);
	public BigDecimal getAmount();
	
	//for CSV output
	//public void renderCSVHeader(PrintWriter out); // static method in the interface might be possible in Java7 (https://docs.google.com/Doc?docid=dfkwr6vq_30dtg2z9d8&hl=en)
	public void renderCSV(PrintWriter out);
}
   
class DeductionSearchResult implements SearchResult {
	Income income;
	Deduction deduction;
	
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	NumberFormat pnf = NumberFormat.getPercentInstance();
	DateFormat df = DateFormat.getDateInstance();
	
	DeductionSearchResult(Income income, Deduction deduction) {
		this.income = income;
		this.deduction = deduction;
	}
	public void render(PrintWriter out) {
		out.write("<tr class=\"data\">");
		out.write("<td width=\"20px\"></td>");
		out.write("<td>"+income.getParent().name+"</td>");
		out.write("<td>"+income.getName()+"</td>");
		out.write("<td>"+deduction.description+"</td>");
		
		out.write("<td style=\"text-align: right;\">");
		if(deduction.isPercentage()) {
			//show percentage as note
			BigDecimal percentage = deduction.getPercentage();
			out.write("<span class=\"note\">(");
			out.write(StringEscapeUtils.escapeHtml((pnf.format(percentage))));
			out.write(")</span> ");
		}
		out.write(StringEscapeUtils.escapeHtml(nf.format(deduction.getAmount(income.getAmount()))));
		out.write("</td>");	
		
		out.write("<td width=\"20px\"></td>");
		out.write("</tr>");
	}
	public String getTerm() {
		return deduction.description;
	}
	static public void renderCSVHeader(PrintWriter out) {
		out.write("income_name,deduction_name,description,amount,note\n");
	}
	@Override
	public void renderCSV(PrintWriter out) {
		out.write("\""+income.getParent().name+"\"");
		out.write(",");
		
		out.write("\""+income.getName()+"\"");
		out.write(",");
		
		out.write("\""+deduction.description+"\"");
		out.write(",");
		
		out.write("\""+nf.format(deduction.getAmount(income.getAmount()))+"\"");	
		out.write(",");
		
		if(deduction.isPercentage()) {
			//show percentage as note
			BigDecimal percentage = deduction.getPercentage();
			out.write("\""+pnf.format(percentage));
			out.write(" of " + income.getName() + " income which was " + nf.format(income.getAmount()));
			out.write("\"");
		}
		out.write("\n");
	}
	@Override
	public BigDecimal getAmount() {
		return deduction.getAmount(income.getAmount());
	}
}

class ExpenseSearchResult implements SearchResult {
	Category category;
	Expense expense;
	
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	NumberFormat pnf = NumberFormat.getPercentInstance();
	DateFormat df = DateFormat.getDateInstance();
	
	ExpenseSearchResult(Category category, Expense expense) {
		this.category = category;
		this.expense = expense;
	}
	public void render(PrintWriter out) {			
		out.write("<tr class=\"data\">");
		out.write("<td width=\"20px\"></td>");
		out.write("<td>"+category.getParent().name+"</td>");
		out.write("<td>"+category.name+"</td>");
		out.write("<td>"+expense.where+"</td>");
		out.write("<td>"+expense.description+"</td>");
		out.write("<td style=\"text-align: right;\" width=\"100px\">"+StringEscapeUtils.escapeHtml(df.format(expense.date))+"</td>");
		out.write("<td style=\"text-align: right;\">"+StringEscapeUtils.escapeHtml(nf.format(expense.amount))+"</td>");
		out.write("<td width=\"20px\"></td>");
		out.write("</tr>");
	}
	public String getTerm() {
		return expense.description + " " + expense.where + " " + category.name;
	}
	
	static public void renderCSVHeader(PrintWriter out) {
		out.write("page_name,category_name,where,description,date,amount,note\n");
	}
	@Override
	public void renderCSV(PrintWriter out) {
		out.write("\""+category.getParent().name+"\"");
		out.write(",");
		
		out.write("\""+category.name+"\"");
		out.write(",");
		
		out.write("\""+expense.where+"\"");
		out.write(",");
		
		out.write("\""+expense.description+"\"");
		out.write(",");
		
		out.write("\""+df.format(expense.date)+"\"");
		out.write(",");
		
		out.write("\""+nf.format(expense.amount)+"\"");	
		out.write(",");
		
		out.write("\n");
	}
	@Override
	public BigDecimal getAmount() {
		return expense.amount;
	}
}
