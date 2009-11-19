package dsbudget.view;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepDate;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

import dsbudget.Main;
import dsbudget.model.Budget;
import dsbudget.model.Category;
import dsbudget.model.Expense;
import dsbudget.model.Income;
import dsbudget.model.Page;

public class RemoveDialog extends DivRepDialog
{
	Budget budget;
	Page current_page;
	Boolean newpage;

	public RemoveDialog(DivRep parent, Budget _budget, Page _current_page) {
		super(parent, true);
		
		budget = _budget;
		current_page = _current_page;
	}
	
	public void onSubmit() {
		//remove balance income that uses the current_page
		for(Page page : budget.pages) {
			ArrayList<Income> new_incomes = new ArrayList<Income>();
			for(Income income : page.incomes) {
				if(income.balance_from != current_page) {
					new_incomes.add(income);
				}
			}
			page.incomes = new_incomes;
		}
		
		//remove the page itself
		budget.pages.remove(current_page);
		
		//handle if there are no more pages left
		Page openpage;
		if(budget.pages.size() == 0) {
			openpage = Main.createEmptyPage(budget);
			budget.pages.add(openpage);
		} 
		openpage = budget.pages.get(0);
		redirect("?page="+openpage.getID());
	}

	public void renderDialog(PrintWriter out) {
		out.write("Do you really want to remove this page?");
	}
	
	public void onCancel() {
		close();	
	}
}