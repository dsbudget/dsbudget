package dsbudget.view;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedHashMap;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

import dsbudget.model.Income;
import dsbudget.model.Page;

public class IncomeDialog extends DivRepDialog
{
	MainView mainview;
	
	public DivRepCheckBox balance_from;
	public DivRepSelectBox balance_from_name;
	public DivRepTextBox amount;
	public DivRepTextBox description;
	
	Income income;
	NumberFormat nf = NumberFormat.getCurrencyInstance();

	public IncomeDialog(MainView parent) {
		super(parent);
		mainview = parent;
		
		setHeight(300);
		setWidth(350);
		
		balance_from = new DivRepCheckBox(this);
		balance_from.setLabel("Use balance from another page");
		balance_from.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				showHideBalance();
			}});
		
		LinkedHashMap<Integer, String> pages_kv = new LinkedHashMap<Integer, String>();
		for(Page page : mainview.getPages()) {
			pages_kv.put(page.getID(), page.name);
		}
		balance_from_name = new DivRepSelectBox(this, pages_kv);
		balance_from_name.setLabel("Balance From");
		balance_from_name.setRequired(true);
		balance_from_name.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
			}
		});
		balance_from_name.addValidator(new DivRepIValidator<Integer>() {
			public String getErrorMessage() {
				return "Circular depencency detected.";
			}

			public Boolean isValid(Integer value) {
				//find the target page
				Page target = mainview.findPage(value);
				if(target.hasBalanceCircle(mainview.page)) {
					return false;
				} else {
					return true;
				}
			}
		});
		
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
			}
		});
		
		description = new DivRepTextBox(this);
		description.setLabel("Name");
		description.setWidth(200);
		description.setRequired(true);		
	}
	
	public void open(Income _income)
	{
		income = _income;
		if(income == null) {
			setTitle("New Income");
			balance_from.setValue(false);
			description.setValue("");
			amount.setValue("");				
		} else {
			setTitle("Update Income");
			if(income.balance_from != null) {
				balance_from.setValue(true);
				balance_from_name.setValue(income.balance_from.getID());
			} else {
				balance_from.setValue(false);
			}
			description.setValue(income.description);
			if(income.amount != null) {
				amount.setValue(nf.format(income.amount));
			}
		}

		showHideBalance();
		
		super.open();
	}
	
	public void showHideBalance()
	{
		Boolean show = balance_from.getValue();
		balance_from_name.setHidden(!show);	
		amount.setHidden(show);
		description.setHidden(show);
		
		balance_from.redraw();
		balance_from_name.redraw();
		description.redraw();
		amount.redraw();
	}
	
	public void onCancel() {
		close();
	}
	public void onSubmit() {
		if(validate()) {
			if(income == null) {
				//new category
				income = new Income(mainview.page);
				income.amount = new BigDecimal(0);
				mainview.page.incomes.add(income);
			}
			
			try {
				if(amount.getValue() == null) {
					income.amount = null;
				} else {
					income.amount = new BigDecimal(nf.parse(amount.getValue()).toString());
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			income.description = description.getValue();
			if(balance_from.getValue()) {
				income.balance_from = mainview.findPage(balance_from_name.getValue());
			} else {
				income.balance_from = null;
			}
			mainview.updateIncomeView();
			close();
			mainview.save();
		}
	}
	public void renderDialog(PrintWriter out) {
		balance_from.render(out);
		
		out.write("<br/>");
		description.render(out);
		amount.render(out);
		balance_from_name.render(out);
	}
	protected Boolean validate()
	{
		Boolean valid = true;
		if(balance_from.getValue()) {
			valid &= balance_from_name.isValid();
		} else {
			valid &= amount.isValid();
			valid &= description.isValid();
		}
		//valid &= deductionview.isValid();
		return valid;
	}
};