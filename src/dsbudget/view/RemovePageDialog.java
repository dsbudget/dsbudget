package dsbudget.view;

import java.util.ArrayList;
import com.divrep.DivRep;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepStaticContent;

import dsbudget.Main;
import dsbudget.i18n.Labels;
import dsbudget.model.Budget;
import dsbudget.model.Income;
import dsbudget.model.Page;

public class RemovePageDialog extends DivRepDialog
{
	Budget budget;
	Page current_page;
	Boolean newpage;

	public RemovePageDialog(DivRep parent, Budget _budget, Page _current_page) {
		super(parent);
		setHasCancelButton(true);
		
		//it is too dangerous to enable enter_to_submit here..
		//setEnterToSubmit(Main.conf.getProperty("enter_to_submit").equals("true"));
		
		setTitle(Labels.getString(RED_LABEL_REMOVE_PAGE));
		budget = _budget;
		current_page = _current_page;
		
		new DivRepStaticContent(this, Labels.getString(RED_LABEL_REMOVE_PAGE_VALIDATION));
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
	
	public static final String RED_LABEL_REMOVE_PAGE = "RemoveDialog.LABEL_REMOVE_PAGE";
	public static final String RED_LABEL_REMOVE_PAGE_VALIDATION = "RemoveDialog.LABEL_REMOVE_PAGE_VALIDATION";

}