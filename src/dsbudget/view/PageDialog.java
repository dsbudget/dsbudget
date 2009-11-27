package dsbudget.view;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepDate;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

import dsbudget.model.Budget;
import dsbudget.model.Category;
import dsbudget.model.Expense;
import dsbudget.model.Income;
import dsbudget.model.Page;

public abstract class PageDialog extends DivRepDialog
{
	Budget budget;
	Page current_page; //current page
	Boolean newpage;
	
	DivRepTextBox title;
	DivRepDate cdate;
	NewPageStuff newpage_stuff;
	
	LinkedHashMap<Integer, String> pages_kv = new LinkedHashMap<Integer, String>();

	class NewPageStuff extends DivRep {
		
		public Boolean hidden = false;
		public DivRepSelectBox copy_from;
		//public DivRepCheckBox usebalance;
		public DivRepSelectBox balance_handling;
		
		public NewPageStuff(DivRep parent) {
			super(parent);
			for(Page page : budget.pages) {
				pages_kv.put(page.getID(), page.name);
			}
			copy_from = new DivRepSelectBox(this, pages_kv);
			copy_from.setNullLabel("(Create an Empty Page)");
			copy_from.setLabel("Copy Income & Budgetting from");
			copy_from.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					usebalanceShowHide();
				}});
			/*
			usebalance = new DivRepCheckBox(this);
			usebalance.setLabel("Add balance from this page as an income");
			usebalance.setValue(true);
			*/
			TreeMap<Integer, String> kv = new TreeMap<Integer, String>();
			kv.put(1, "Sum up and add as an income");
			kv.put(2, "Add to each categories as negative expenses");
			balance_handling = new DivRepSelectBox(this, kv);
			balance_handling.setNullLabel("(Do Nothing)");
			balance_handling.setLabel("What do you wan to do with the balance?");
		}
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			if(!hidden) {
				out.write("<br/>");
				copy_from.render(out);
				/*
				if(copySourceHasAutoAdjust()) {
					out.write("<div class=\"round4\" style=\"background-color: #ccc;padding: 5px; margin: 5px;\">");
					out.write("Selected page contains categories that are marked as 'Rollover Category'. Page balance will be added as income in order for the adjustments to be valid.");
					out.write("</div>");
				} else {
					usebalance.render(out);
				}
				usebalance.render(out);
				*/
				balance_handling.render(out);
			}
			out.write("</div>");
		}
		/*
		public Boolean copySourceHasAutoAdjust()
		{
			Integer id = copy_from.getValue();
			if(id == null) return false;
			
			Page copy_from = budget.findPage(id);
			Boolean auto_adjust = false;
			for(Category cat : copy_from.categories) {
				if(cat.auto_adjust) {
					auto_adjust = true;
					break;
				}
			}
			return auto_adjust;
		}
		 */
		public void usebalanceShowHide()
		{
			Integer id = copy_from.getValue();
			if(id == null || id.equals("")) {
				balance_handling.setHidden(true);
				balance_handling.setValue(null);
			} else {
				balance_handling.setHidden(false);	
			}
			redraw();
		}
	
	};
	
	public void open() {
		throw new RuntimeException("please use open() with boolean");
	}
	
	public void open(Boolean _newpage) {
		newpage = _newpage;
		
		if(newpage) {
			setTitle("Create New Page");
			
			//set new name
			Date today = new Date();     
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");  
			String monthName = dateFormat.format(today);  
			String newname = monthName;
			title.setValue(newname);
			cdate.setValue(new Date()); //use today's date
			
			newpage_stuff.copy_from.setValue(current_page.getID()); //copy from current page by default
			newpage_stuff.hidden = false;
			//newpage_stuff.usebalanceShowHide();
		} else {
			//update current page settings
			setTitle("Page Settings");	
			
			title.setValue(current_page.name);
			cdate.setValue(current_page.created);
			
			newpage_stuff.hidden = true;
		}
		title.redraw();
		cdate.redraw();
		newpage_stuff.redraw();
		
		super.open();
	}
	
	public PageDialog(DivRep parent, Budget _budget, Page _current_page) {
		super(parent, true);
		
		setHeight(350);
		setWidth(400);	
		
		budget = _budget;
		current_page = _current_page;

		title = new DivRepTextBox(this);
		title.setLabel("Title");
		title.setWidth(220);
				
		title.setRequired(true);
		title.addValidator(new DivRepIValidator<String>() {
			public String getErrorMessage() {
				return "A page with the same title already exists";
			}

			public Boolean isValid(String value) {
				if(newpage) {
					for(Page page : budget.pages) {
						if(page.name.equals(value)) {
							return false;
						}
					}
				}
				return true;
			}
		});
		
		cdate = new DivRepDate(this);
		cdate.setLabel("Graph Beginning Date");
		cdate.setRequired(true);

		newpage_stuff = new NewPageStuff(this);
	}
	
	public void onSubmit() {
		if(isValid()) {
			if(newpage) {
				createNewPage();
			} else {
				updatePage();
			}
			budget.save();
		}
	}
	
	private void updatePage()
	{
		current_page.name = title.getValue();
		current_page.created = cdate.getValue();
		redirect("?page="+current_page.getID());
	}
	
	private void createNewPage()
	{
		Page newpage;
		
		Integer id = newpage_stuff.copy_from.getValue();
		if(id != null) {
			//copy from the original page
			Page original = budget.findPage(id);
			newpage = original.clone();
			
			
			//clear balance income
			ArrayList<Income> non_balance_incomes = new ArrayList<Income>();
			for(Income income : newpage.incomes) {
				if(income.balance_from == null)  {
					non_balance_incomes.add(income);
				}
			}
			newpage.incomes = non_balance_incomes;
			
			
			//add balance as income
			Integer action = newpage_stuff.balance_handling.getValue();
			if(action == null) {
				//do nothing.. just clear all expenses
				for(Category category : newpage.categories) {
					category.expenses = new ArrayList<Expense>();
				}
			} else if(action.equals(1)) {
				//add as income
				Income income = new Income(newpage);
				income.balance_from = original;
				newpage.incomes.add(income);
				
				//then clear all expenses
				for(Category category : newpage.categories) {
					category.expenses = new ArrayList<Expense>();
				}
			} else if(action.equals(2)) {
				for(Category category : newpage.categories) {
					BigDecimal balance = category.amount;
					balance = balance.subtract(category.getTotalExpense());
					
					category.expenses = new ArrayList<Expense>();
					Expense balance_expense = new Expense();
					balance_expense.amount = balance.negate();
					balance_expense.date = newpage.created;
					balance_expense.where = "(Balance from " + original.name + ")";
					balance_expense.description = "";
					category.expenses.add(balance_expense);
				}
			}
			
	/*	
			//run budget auto-adjust
			for(Category category : newpage.categories) {
				if(category.auto_adjust) {
					BigDecimal balance = category.amount;
					balance = balance.subtract(category.getTotalExpense());
					category.amount = category.amount.add(balance);
				}
			}
			*/	
			
	
			
		} else {
			//empty page
			newpage = new Page(budget);
		}
		newpage.name = title.getValue();
		newpage.created = cdate.getValue();
		budget.pages.add(newpage);

		redirect("?page="+newpage.getID());
	}
	
	public Boolean isValid()
	{
		Boolean valid = true;
		valid &= title.isValid();
		return valid;
	}

	public void renderDialog(PrintWriter out) {
		title.render(out);
		cdate.render(out);
		newpage_stuff.render(out);
	}
}