package dsbudget.view;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepDate;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextBox;

import dsbudget.model.Category;
import dsbudget.model.Expense;

public class ExpenseDialog extends DivRepDialog
{
	MainView mainview;
	
	ExpenseDialogContent content;
	
	public DivRepTextBox where;
	public DivRepTextBox note;
	public DivRepDate date;
	public DivRepTextBox amount;
	public DivRepCheckBox tentative;
	
	Category category;
	Expense expense;
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	
	class ExpenseDialogContent extends DivRep {

		public ExpenseDialogContent(DivRep parent) {
			super(parent);
			where = new DivRepTextBox(this);
			where.setLabel("Where");
			where.setWidth(200);
			where.setRequired(true);
			
			note = new DivRepTextBox(this);
			note.setLabel("Note");
			note.setWidth(300);
			
			date = new DivRepDate(this);
			date.setLabel("Date");
			date.setRequired(true);
			
			amount = new DivRepMoneyAmount(this);
			amount.setLabel("Amount");
			amount.setWidth(200);
			amount.setSampleValue(nf.format(10));
			amount.setRequired(true);
			/*
			amount.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					String value = e.value.trim();
					amount.setValue("");
					try {
						BigDecimal b = new BigDecimal(value);
						amount.setValue(nf.format(b));
					} catch(NumberFormatException ne) {
						try {
							Number n = nf.parse(value);
							amount.setValue(nf.format(n));
						} catch (ParseException e1) {
							//any other idea?
						}
					}
					amount.redraw();
				}
			});
			*/
			
			/////////////////////////////////////////////////////////////
			//
			// Following are some optional stuff (that should probably hidden)
			//
			tentative = new DivRepCheckBox(this);
			tentative.setLabel("This is a scheduled (tentative) expense. Don't subtract it from the remaining.");	
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			where.render(out);
			note.render(out);
			date.render(out);
			amount.render(out);
			
			out.write("<div class=\"optional_section round4\">");
			tentative.render(out);
			out.write("</div>");
			
			out.write("</div>");			
		}
		
	}
	
	public ExpenseDialog(MainView parent) {
		super(parent);
		mainview = parent;
		
		setHeight(400);
		setWidth(370);
		
		content = new ExpenseDialogContent(this);
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
			tentative.setValue(false);
		} else {
			setTitle("Update Expense - " + category.name);
			where.setValue(expense.where);
			amount.setValue(nf.format(expense.amount));
			note.setValue(expense.description);
			date.setValue(expense.date);
			tentative.setValue(expense.tentative);
			
			where.validate();
			amount.validate();
			note.validate();
			date.validate();
			tentative.validate();
		}
		
		where.redraw();
		amount.redraw();
		note.redraw();
		date.redraw();
		tentative.redraw();
		super.open();
	}
	
	public void onCancel() {
		date.close();
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
				Number parsed = nf.parse(amount.getValue());
				BigDecimal bd = new BigDecimal(parsed.toString());
				expense.amount = bd;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			expense.where = where.getValue();
			expense.description = note.getValue();
			expense.date = date.getValue();
			expense.tentative = tentative.getValue();
			
			close();
			mainview.updateExpenseCategory(category);
			mainview.save();
		}
	}

	protected Boolean validate()
	{
		Boolean valid = true;
		valid &= where.isValid();
		valid &= note.isValid();
		valid &= date.isValid();
		valid &= amount.isValid();
		valid &= tentative.isValid();
		return valid;
	}
};
