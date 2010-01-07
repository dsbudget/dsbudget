package dsbudget.view;

import java.io.PrintWriter;
import java.util.ArrayList;
import com.divrep.DivRep;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepStaticContent;

import dsbudget.Main;
import dsbudget.model.Budget;
import dsbudget.model.Income;
import dsbudget.model.Page;

public class RemoveDialog extends DivRepDialog
{
	Budget budget;
	Page current_page;
	Boolean newpage;

	public RemoveDialog(DivRep parent, Budget _budget, Page _current_page) {
		super(parent, true);
		
		setTitle("Remove Page");
		budget = _budget;
		current_page = _current_page;
		
		new DivRepStaticContent(this, "Do you really want to remove this page?");
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
		close();
		redirect("?page="+openpage.getID());
		budget.save();
	}
	
	public void onCancel() {
		close();	
	}
}