package dsbudget.view;

import java.awt.Color;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepTextBox;

import dsbudget.model.Category;

public class CategoryDialog extends DivRepDialog
{
	MainView mainview;
	
	public DivRepTextBox name;
	public DivRepTextBox description;
	public DivRepTextBox amount;
	public DivRepColorPicker color;
	
	Category category;
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	
	public CategoryDialog(MainView parent) {
		super(parent);
		mainview = parent;
	
		name = new DivRepTextBox(this);
		name.setLabel("Name");
		name.setWidth(200);
		name.setRequired(true);
		name.setSampleValue("Mortgage");
		
		amount = new DivRepTextBox(this);
		amount.setLabel("Budget");
		amount.setWidth(200);
		amount.setSampleValue(nf.format(700));
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
		description.setLabel("Description");
		description.setWidth(290);
		
		color = new DivRepColorPicker(this);
		color.setLabel("Color");
	}
	
	public void open(Category _category)
	{
		category = _category;
		if(category == null) {
			setTitle("New Bucket");
			name.setValue("");
			description.setValue("");
			amount.setValue("");	
			color.setValue(Color.blue);
		} else {
			setTitle("Update Bucket");
			name.setValue(category.name);
			description.setValue(category.description);
			amount.setValue(nf.format(category.amount));
			color.setValue(category.color);
		}
		
		name.redraw();
		description.redraw();
		amount.redraw();
		color.redraw();
		
		super.open();
	}
	
	public void onCancel() {
		close();
	}
	public void onSubmit() {
		if(validate()) {
			if(category == null) {
				//new category
				category = new Category(mainview.page);
				mainview.page.categories.add(category);
				
				category.fixed = false;
				category.hide_graph = true;
			}
			
			try {
				category.amount = new BigDecimal(nf.parse(amount.getValue()).doubleValue());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			category.name = name.getValue();
			category.description = description.getValue();
			category.color = color.getValue();
			
			mainview.redraw();
			mainview.initView();
			close();
		}
	}
	public void renderDialog(PrintWriter out) {
		name.render(out);
		amount.render(out);
		description.render(out);
		color.render(out);
	}
	protected Boolean validate()
	{
		Boolean valid = true;
		valid &= name.isValid();
		valid &= description.isValid();
		valid &= amount.isValid();
		valid &= color.isValid();
		return valid;
	}
};