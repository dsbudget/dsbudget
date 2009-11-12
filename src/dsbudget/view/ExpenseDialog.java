package dsbudget.view;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepDate;
import com.divrep.common.DivRepTextBox;

import dsbudget.model.Category;
import dsbudget.model.Expense;

public class ExpenseDialog extends DivRepDialog
{
	MainView mainview;
	
	public DivRepTextBox where;
	public DivRepTextBox note;
	public DivRepDate date;
	public DivRepTextBox amount;
	
	Category category;
	Expense expense;
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	
	public ExpenseDialog(MainView parent) {
		super(parent);
		mainview = parent;
		
		setHeight(400);
		setWidth(370);
	
		where = new DivRepTextBox(this);
		where.setLabel("Where");
		where.setWidth(200);
		//where.setSampleValue("A place for this expense");
		where.setRequired(true);
		
		note = new DivRepTextBox(this);
		note.setLabel("Note");
		note.setWidth(300);
		
		date = new DivRepDate(this);
		date.setLabel("Date");
		date.setRequired(true);
		
		amount = new DivRepTextBox(this);
		amount.setLabel("Amount");
		amount.setWidth(200);
		amount.setSampleValue(nf.format(10));
		amount.setRequired(true);
		amount.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				amount.setValue("");
				try {
					BigDecimal b = new BigDecimal(e.value);
					amount.setValue(nf.format(b));
				} catch(NumberFormatException ne) {
					try {
						Number n = nf.parse(e.value);
						amount.setValue(nf.format(n));
					} catch (ParseException e1) {
						//any other idea?
					}

				}
				amount.redraw();
			}});
	}
	
	public void open(Category _category, Expense _expense)
	{
		category = _category;
		expense = _expense;
		if(expense == null) {
			setTitle("New Expense - " + category.name);
			where.setValue("");
			amount.setValue("");
			note.setValue("");
			date.setValue(new Date());
		} else {
			setTitle("Update Expense - " + category.name);
			where.setValue(expense.where);
			amount.setValue(nf.format(expense.amount));
			note.setValue(expense.description);
			date.setValue(expense.date);
			
			where.validate();
			amount.validate();
			note.validate();
			date.validate();
		}
		where.redraw();
		amount.redraw();
		note.redraw();
		date.redraw();
		super.open();
	}
	
	public void onCancel() {
		close();
	}
	public void onSubmit() {
		if(validate()) {
			if(expense == null) {
				//new expense
				expense = new Expense();
				category.addExpense(expense);
			}
			
			try {
				expense.amount = new BigDecimal(nf.parse(amount.getValue()).doubleValue());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			expense.where = where.getValue();
			expense.description = note.getValue();
			expense.date = date.getValue();
			
			close();
			mainview.updateExpenseCategory(category);
		}
	}
	public void renderDialog(PrintWriter out) {
		where.render(out);
		amount.render(out);
		date.render(out);
		note.render(out);
	}
	protected Boolean validate()
	{
		Boolean valid = true;
		valid &= where.isValid();
		valid &= note.isValid();
		valid &= date.isValid();
		valid &= amount.isValid();
		return valid;
	}
};
