package dsbudget.view;

import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepStaticContent;
import dsbudget.i18n.Labels;
import dsbudget.model.Income;

public class RemoveIncomeDialog extends DivRepDialog
{
	Income income;
	MainView mainview;
	
	public RemoveIncomeDialog(MainView mainview) {
		super(mainview);
		this.mainview = mainview;
		
		setWidth(300);
		setHasCancelButton(true);
		//it is too dangerous to enable enter_to_submit here..
		//setEnterToSubmit(Main.conf.getProperty("enter_to_submit").equals("true"));
		
		new DivRepStaticContent(this, Labels.getString("RemoveDialog.LABEL_REMOVE_INCOME_VALIDATION"));
	}
	
	public void open(Income income) {
		this.income = income;
		setTitle(Labels.getString("RemoveDialog.LABEL_REMOVE_INCOME", income.description));
		super.open();
	}
	
	public void onSubmit() {
		mainview.removeIncome(income);
		mainview.save();
		close();
	}
	
	public void onCancel() {
		close();	
	}
}