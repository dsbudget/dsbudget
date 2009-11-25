package dsbudget.view;

import java.awt.Color;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepColorPicker;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

import dsbudget.model.Category;
import dsbudget.model.Page;

public class CategoryDialog extends DivRepDialog
{
	MainView mainview;
	
	public DivRepTextBox name;
	public DivRepTextBox description;
	public DivRepTextBox amount;
	public DivRepColorPicker color;
	//public DivRepCheckBox auto_adjust;
	
	Category category;
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	
	public CategoryDialog(MainView parent) {
		super(parent);
		mainview = parent;
		
		setHeight(460);
		setWidth(450);
	
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
		amount.addValidator(new DivRepIValidator<String>(){
			public String getErrorMessage() {
				return "Please use a positive amount.";
			}

			public Boolean isValid(String value) {
				try {
					BigDecimal a = new BigDecimal(nf.parse(value).doubleValue());
					if(a.compareTo(BigDecimal.ZERO) < 0) {
						return false;
					}
				} catch (NumberFormatException ne) {
					//ignore then
				} catch (ParseException e) {
					//ignore then
				}
				return true;
			}});
		
		description = new DivRepTextBox(this);
		description.setLabel("Note");
		description.setWidth(290);
		
		color = new DivRepColorPicker(this);
		color.setLabel("Color");

		for(Page page : mainview.getPages()) {
			for(Category category : page.categories) {
				color.addPresetColor(category.color);
			}
		}
		
		//auto_adjust = new DivRepCheckBox(this);
		//auto_adjust.setLabel("This is a rollover category");
	}
	
	public void open(Category _category)
	{
		category = _category;
		if(category == null) {
			setTitle("New Category");
			name.setValue("");
			description.setValue("");
			amount.setValue("");	
			color.setValue(Color.blue);
			//auto_adjust.setValue(false);
		} else {
			setTitle("Update Category");
			name.setValue(category.name);
			description.setValue(category.description);
			amount.setValue(nf.format(category.amount));
			color.setValue(category.color);
			//auto_adjust.setValue(category.auto_adjust);
		}
		
		name.redraw();
		description.redraw();
		amount.redraw();
		color.redraw();
		//auto_adjust.redraw();
		
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
			//category.auto_adjust = auto_adjust.getValue();
			
			mainview.redraw();
			mainview.initView();
			close();
			
			//add current color to preset
			color.addPresetColor(color.getValue());
		}
	}
	public void renderDialog(PrintWriter out) {
		name.render(out);
		amount.render(out);
/*
		out.write("<div style=\"background-color: #ccc; padding: 10px; margin-bottom: 5px;\" class=\"round4\">");
		//auto_adjust.render(out);
		out.write("<br/><p>Please check this if you want the balance of this category to be added to the budget for the same category when you open a new page. This is commonly used for saving categories.</p>");
		out.write("</div>");
		*/	
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
		//valid &= auto_adjust.isValid();
		return valid;
	}
};