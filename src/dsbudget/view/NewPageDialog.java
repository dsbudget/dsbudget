package dsbudget.view;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import com.divrep.DivRep;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

import dsbudget.model.Budget;
import dsbudget.model.Category;
import dsbudget.model.Expense;
import dsbudget.model.Income;
import dsbudget.model.Page;

public class NewPageDialog extends DivRepDialog
{
	Budget budget;
	Page page; //current page
	
	DivRepTextBox title;
	DivRepSelectBox copy_from;
	LinkedHashMap<Integer, String> pages_kv = new LinkedHashMap<Integer, String>();
	
	public NewPageDialog(DivRep parent, Budget _budget, Page _page) {
		super(parent, true);
		
		budget = _budget;
		page = _page;
		
		setTitle("Create New Page");
		setHeight(250);
		setWidth(400);
		
		Date today = new Date();     
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");  
		String monthName = dateFormat.format(today);  
		String newname = monthName;

		title = new DivRepTextBox(this);
		title.setLabel("Title");
		title.setWidth(220);
		title.setValue(newname);
		title.setRequired(true);
		title.addValidator(new DivRepIValidator<String>() {
			public String getErrorMessage() {
				return "A page with the same title already exists";
			}

			public Boolean isValid(String value) {
				for(Page page : budget.pages) {
					if(page.name.equals(value)) {
						return false;
					}
				}
				return true;
			}
		});
		
		for(Page page : budget.pages) {
			pages_kv.put(page.getID(), page.name);
		}
		copy_from = new DivRepSelectBox(this, pages_kv);
		copy_from.setNullLabel("(Empty Page)");
		copy_from.setLabel("Copy Income & Budgetting from");
		copy_from.setValue(page.getID());
	}

	public void onCancel() {
		close();
	}

	public void onSubmit() {
		if(isValid()) {
			Page newpage;
			
			//copy from other page if requested
			Integer id = copy_from.getValue();
			if(id != null) {
				newpage = page.clone();
				
				//clear expenses
				for(Category category : newpage.categories) {
					category.expenses = new ArrayList<Expense>();
				}
				
				//clear balance income
				ArrayList<Income> non_balance_incomes = new ArrayList<Income>();
				for(Income income : newpage.incomes) {
					if(income.balance_from == null)  {
						non_balance_incomes.add(income);
					}
				}
				newpage.incomes = non_balance_incomes;
				
			} else {
				newpage = new Page(budget);
			}
			newpage.name = title.getValue();
			budget.pages.add(newpage);

			redirect("?page="+newpage.getID());
		}
	}
	
	public Boolean isValid()
	{
		Boolean valid = true;
		valid &= title.isValid();
		return valid;
	}

	public void renderDialog(PrintWriter out) {
		title.render(out);
		copy_from.render(out);
		out.write("<p>* Balance Incomes and Expenses will not be copied</p>");
	}
}