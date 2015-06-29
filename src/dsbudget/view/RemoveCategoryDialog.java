package dsbudget.view;

import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepStaticContent;
import dsbudget.i18n.Labels;
import dsbudget.model.Category;

public class RemoveCategoryDialog extends DivRepDialog
{
	Category category;
	MainView mainview;
	
	public RemoveCategoryDialog(MainView mainview) {
		super(mainview);
		this.mainview = mainview;
		
		setWidth(300);
		setHasCancelButton(true);
		//it is too dangerous to enable enter_to_submit here..
		//setEnterToSubmit(Main.conf.getProperty("enter_to_submit").equals("true"));
		
		new DivRepStaticContent(this, Labels.getString("RemoveDialog.LABEL_REMOVE_CATEGORY_VALIDATION"));
	}
	
	public void open(Category category) {
		this.category = category;
		setTitle(Labels.getString("RemoveDialog.LABEL_REMOVE_CATEGORY", category.name));
		super.open();
	}
	
	public void onSubmit() {
		mainview.removeCategory(category);
		mainview.save();
		close();
	}
	
	public void onCancel() {
		close();	
	}
}