package dsbudget.view;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;

import dsbudget.model.Budget;
import dsbudget.model.Category;
import dsbudget.model.Expense;
import dsbudget.model.Income;
import dsbudget.model.Page;

public class MainView extends DivRep {
	Budget budget;
	Page page;
	
	public IncomeView incomeview;
	public BudgetingView budgettingview;
	public ExpenseView expenseview;
	
	//dialogs
	IncomeDialog income_dialog;
	CategoryDialog category_dialog;
	ExpenseDialog expense_dialog;
	DeductionDialog deduction_dialog;
	
	public MainView(DivRep parent, Budget _budget, Page _page) {
		super(parent);
		page = _page;
		budget = _budget;

		income_dialog = new IncomeDialog(this); 
		category_dialog = new CategoryDialog(this); 
		expense_dialog = new ExpenseDialog(this); 
		deduction_dialog = new DeductionDialog(this);
		
		initView();
	}
	public void initView()
	{
		incomeview = new IncomeView(this);
		budgettingview = new BudgetingView(this);
		expenseview = new ExpenseView(this);
		/*
		//handle category slider change
		budgettingview.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				expenseview.updateExpenseCategory(e.value);
			}
		});
		*/
	}
	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
	
		incomeview.render(out);
		budgettingview.render(out);
		expenseview.render(out);
		
		income_dialog.render(out);
		category_dialog.render(out);
		expense_dialog.render(out);
		deduction_dialog.render(out);
		
		out.write("</div>");
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	//view updaters -- TODO -- the design of this is all wrong.... we should be dealing with
	//update the views - not actually changing the model.
	public void addCategory(Category cat) {
		page.categories.add(cat);
		initView();
		redraw();
	}
	public void removeCategory(Category cat) {
		page.categories.remove(cat);
		initView();
		redraw();
	}
	public void removeIncome(Income in) {
		page.incomes.remove(in);
		incomeview.redraw();
		budgettingview.redraw();
	}
	public void removeExpense(Category cat, Expense ex) {
		cat.removeExpense(ex);
		expenseview.updateExpenseCategory(cat);
	}
	public void updateCategory(Category cat) {
		budgettingview.redraw();
		expenseview.redraw();
	}
	public void updateExpenseCategory(Category cat) {
		expenseview.updateExpenseCategory(cat);
	}
	public void updateIncomeView()
	{
		incomeview.redraw();
		budgettingview.redraw();
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//getter / setter proxy
	public ArrayList<Income> getIncomes() {
		return page.incomes;
	}
	public ArrayList<Category> getCategories() {
		/*
		ArrayList<Category> sorted = page.categories;
		Collections.sort(sorted, new Comparator<Category> () {
			public int compare(Category a, Category b) {
				return a.name.compareTo(b.name);
			}
		});
		return sorted;
		*/
		return page.categories;
	}
	public void setCategories(ArrayList<Category> list) {
		page.categories = list;
		expenseview.initView();
	}
	public Integer getPageID() {
		return page.getID();
	}
	public BigDecimal getTotalIncomeDeduction() {
		return page.getTotalIncomeDeduction();
	}
	public BigDecimal getTotalBudgetted() {
		return page.getTotalBudgetted();
	}
	public BigDecimal getTotalUnBudgetted() {
		BigDecimal total_free_income = getTotalIncome();
		total_free_income = total_free_income.subtract(getTotalIncomeDeduction());
		return total_free_income.subtract(getTotalBudgetted());
	}
	public BigDecimal getTotalIncome() {
		return page.getTotalIncome();
	}
	public ArrayList<Page> getPages() {
		return budget.pages;
	}
	public Page findPage(Integer pageid) {
		return budget.findPage(pageid);
	}
	public void save() {
		budget.save();
	}
}