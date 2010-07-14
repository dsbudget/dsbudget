package dsbudget.view;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jfree.util.Log;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepDate;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepMoneyAmount;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextBox;

import dsbudget.Main;
import dsbudget.i18n.Labels;
import dsbudget.model.Category;
import dsbudget.model.Expense;

public class ExpenseDialog extends DivRepDialog
{
	static Logger logger = Logger.getLogger(ExpenseDialog.class);
	MainView mainview;
	
	ExpenseDialogContent content;
	
	public DivRepTextBox where;
	public DivRepTextBox note;
	public DivRepDate date;
	public DivRepTextBox amount;
	public DivRepCheckBox tentative;
	public DivRepSelectBox moveto;
	
	Category category;
	Expense expense;
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	
	class ExpenseDialogContent extends DivRep {

		public ExpenseDialogContent(DivRep parent) {
			super(parent);
			where = new DivRepTextBox(this);
			where.setLabel(Labels.getString(EXD_LABEL_WHERE));
			where.setWidth(200);
			where.setRequired(true);
			
			note = new DivRepTextBox(this);
			note.setLabel(Labels.getString(EXD_LABEL_NOTE));
			note.setWidth(300);
			
			date = new DivRepDate(this);
			date.setLabel(Labels.getString(EXD_LABEL_DATE));
			date.setRequired(true);
			
			amount = new DivRepMoneyAmount(this);
			amount.setLabel(Labels.getString(EXD_LABEL_AMOUNT));
			amount.setWidth(200);
			amount.setSampleValue(nf.format(Integer.valueOf(Labels.getString(EXD_LABEL_AMOUNT_SAMPLE))));
			amount.setRequired(true);
			
			/////////////////////////////////////////////////////////////
			//
			// Following are some optional stuff (that should probably hidden)
			//
			tentative = new DivRepCheckBox(this);
			tentative.setLabel(Labels.getString(EXD_LABEL_TENTATIVE));	
			
			moveto = new DivRepSelectBox(this);
			moveto.setLabel(Labels.getString("ExpenseDialog.LABEL_MOVE"));
			moveto.setNullLabel(Labels.getString("ExpenseDialog.LABEL_NOMOVE"));
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			where.render(out);
			amount.render(out);
			date.render(out);
			note.render(out);
		
			out.write("<div class=\"optional_section round4\">");
			tentative.render(out);
			out.write("<br>");
			moveto.render(out);
			out.write("</div>");
			
			out.write("</div>");			
		}
	}
	
	public ExpenseDialog(MainView parent) {
		super(parent);
		mainview = parent;
		
		setHeight(430);
		setWidth(370);
		setEnterToSubmit(Main.conf.getProperty("enter_to_submit").equals("true"));
		
		content = new ExpenseDialogContent(this);
	}
	
	public void open(Category _category, Expense _expense)
	{
		category = _category;
		expense = _expense;
		if(expense == null) {
			setTitle(Labels.getString(EXD_LABEL_NEW_EXPENSE, category.name));
			where.setValue("");
			amount.setValue("");
			note.setValue("");
			date.setValue(new Date());
			tentative.setValue(false);
			moveto.setHidden(true);
		} else {
			setTitle(Labels.getString(EXD_LABEL_UPDATE_EXPENSE, category.name));
			where.setValue(expense.where);
			amount.setValue(nf.format(expense.amount));
			note.setValue(expense.description);
			date.setValue(expense.date);
			tentative.setValue(expense.tentative);
			
			moveto.setHidden(false);
			LinkedHashMap<Integer, String> kv = new LinkedHashMap<Integer, String>();
			for(Category cat : mainview.page.categories) {
				if(cat == category) continue;
				kv.put(cat.getID(), cat.name);
			}
			moveto.setValues(kv);
			moveto.setValue(null);
			
			where.validate();
			amount.validate();
			note.validate();
			date.validate();
			tentative.validate();
			moveto.validate();
		}
		
		//TODO - reset autocomplete values for where field based
		HashSet<String> values = new HashSet<String>();
		for(Expense expense : category.expenses) {
			values.add(expense.where.trim());
		}
		where.setAutoCompleteValues(values);
		
		where.redraw();
		amount.redraw();
		note.redraw();
		date.redraw();
		tentative.redraw();
		moveto.redraw();
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
			
			Integer moveto = this.moveto.getValue();
			if(moveto != null) {
				for(Category cat : mainview.page.categories) {
					if(cat.getID().equals(moveto)) {
						category.removeExpense(expense);
						cat.addExpense(expense);
						mainview.updateCategory(cat);
						break;
					}
				}
			}
			
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
	
	public static final String EXD_LABEL_WHERE = "ExpenseDialog.LABEL_WHERE";
	public static final String EXD_LABEL_NOTE = "ExpenseDialog.LABEL_NOTE";
	public static final String EXD_LABEL_DATE = "ExpenseDialog.LABEL_DATE";
	public static final String EXD_LABEL_AMOUNT = "ExpenseDialog.LABEL_AMOUNT";
	public static final String EXD_LABEL_AMOUNT_SAMPLE = "ExpenseDialog.LABEL_AMOUNT_SAMPLE";
	public static final String EXD_LABEL_TENTATIVE = "ExpenseDialog.LABEL_TENTATIVE";
	public static final String EXD_LABEL_NEW_EXPENSE = "ExpenseDialog.LABEL_NEW_EXPENSE";
	public static final String EXD_LABEL_UPDATE_EXPENSE = "ExpenseDialog.LABEL_UPDATE_EXPENSE";

};
