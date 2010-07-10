package dsbudget.view;

import java.awt.Color;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedHashMap;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepColorPicker;
import com.divrep.common.DivRepDate;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepMoneyAmount;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

import dsbudget.Main;
import dsbudget.i18n.Labels;
import dsbudget.model.Category;
import dsbudget.model.Page;
import dsbudget.model.Category.SortBy;
import dsbudget.view.ExpenseDialog.ExpenseDialogContent;

public class CategoryDialog extends DivRepDialog
{
	MainView mainview;
	
	CategoryDialogContent content;
	
	public DivRepTextBox name;
	public DivRepTextBox description;
	public DivRepTextBox amount;
	public DivRepColorPicker color;
	
	public DivRepSelectBox sort_by;
	public DivRepCheckBox sort_reverse;
	
	Category category;
	NumberFormat nf = NumberFormat.getCurrencyInstance();
	
	class CategoryDialogContent extends DivRep {

		public CategoryDialogContent(DivRep parent) {
			super(parent);
			
			name = new DivRepTextBox(this);
			name.setLabel(Labels.getString(CAD_LABEL_NAME));
			name.setWidth(200);
			name.setRequired(true);
			name.setSampleValue(Labels.getString(CAD_LABEL_NAME_SAMPLE));
			
			amount = new DivRepMoneyAmount(this);
			amount.setLabel(Labels.getString(CAD_BUDGET_NAME));
			amount.setWidth(200);
			amount.setSampleValue(nf.format(Integer.valueOf(Labels.getString(CAD_BUDGET_SAMPLE))));
			amount.setRequired(true);
			amount.addValidator(new DivRepIValidator<String>(){
				public String getErrorMessage() {
					return Labels.getString(CAD_MESSAGE_USE_POSITIVE_AMOUNT);
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
			description.setLabel(Labels.getString(CAD_LABEL_NOTE));
			description.setWidth(290);
			
			color = new DivRepColorPicker(this);
			color.setLabel(Labels.getString(CAD_LABEL_COLOR));

			for(Page page : mainview.getPages()) {
				for(Category category : page.categories) {
					color.addPresetColor(category.color);
				}
			}
			
	        LinkedHashMap<Integer, String> options = new LinkedHashMap<Integer, String>();
	        options.put(1, Labels.getString("CategoryDialog.OPTION_SORTBY_WHERE"));
	        options.put(2, Labels.getString("CategoryDialog.OPTION_SORTBY_DATE"));
	        options.put(3, Labels.getString("CategoryDialog.OPTION_SORTBY_AMOUNT"));
			sort_by = new DivRepSelectBox(this, options);
			sort_by.setHasNull(false);
			
			sort_reverse = new DivRepCheckBox(this);
			sort_reverse.setLabel(Labels.getString("CategoryDialog.LABEL_SORT_REVERSE"));
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			
			name.render(out);
			amount.render(out);	
			description.render(out);
			color.render(out);
			
			out.write("<div class=\"optional_section round4\">");
			
			out.write("<table><tr>");
			out.write("<td>");
			out.write(Labels.getString("CategoryDialog.LABEL_EXPENSE_SORT_OPTION"));
			out.write("</td><td>");
			sort_by.render(out);
			out.write("</td><td>");
			sort_reverse.render(out);
			out.write("</td></tr></table>");

			out.write("</div>");
			
			out.write("</div>");			
		}
		
	}
	
	public CategoryDialog(MainView parent) {
		super(parent);
		mainview = parent;
		
		setHeight(540);
		setWidth(450);
		setEnterToSubmit(Main.conf.getProperty("enter_to_submit").equals("true"));
		
		content = new CategoryDialogContent(this);
	}
	
	private int convertToIndex(SortBy by) {
		switch(by) {
		case WHERE: return 1;
		case DATE: return 2;
		case AMOUNT: return 3;
		}
		return -1;
	}
	private SortBy toSortBy(int id) {
		switch(id) {
		case 1: return SortBy.WHERE;
		case 2: return SortBy.DATE;
		case 3: return SortBy.AMOUNT;
		}
		return null;
	}

	public void open(Category _category)
	{
		category = _category;
		if(category == null) {
			setTitle(Labels.getString(CAD_LABEL_NEW_CATEGORY));
			name.setValue("");
			description.setValue("");
			amount.setValue("");	
			color.setValue(Color.blue);
			sort_by.setValue(2);
			sort_reverse.setValue(false);
		} else {
			setTitle(Labels.getString(CAD_LABEL_UPDATE_CATEGORY));
			name.setValue(category.name);
			description.setValue(category.description);
			amount.setValue(nf.format(category.amount));
			color.setValue(category.color);
			sort_by.setValue(convertToIndex(category.sort_by));
			sort_reverse.setValue(category.sort_reverse);
		}
		
		name.redraw();
		description.redraw();
		amount.redraw();
		color.redraw();
		sort_by.redraw();
		sort_reverse.redraw();
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
				category.hide_balance_graph = true;
				category.hide_pie_graph = true;
			}
			
			try {
				category.amount = new BigDecimal(nf.parse(amount.getValue()).toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			category.name = name.getValue();
			category.description = description.getValue();
			category.color = color.getValue();
			category.sort_by = toSortBy(sort_by.getValue());
			category.sort_reverse = sort_reverse.getValue();
			//category.graph_type = toGraphType(graph_type.getValue());
			
			mainview.redraw();
			mainview.initView();
			close();
			
			//add current color to preset
			color.addPresetColor(color.getValue());
			mainview.save();
		}
	}
	
	protected Boolean validate()
	{
		Boolean valid = true;
		valid &= name.isValid();
		valid &= description.isValid();
		valid &= amount.isValid();
		valid &= color.isValid();
		valid &= sort_by.isValid();
		valid &= sort_reverse.isValid();
		//valid &= graph_type.isValid();
		return valid;
	}
	
	public static final String CAD_LABEL_NAME = "CategoryDialog.LABEL_NAME";
	public static final String CAD_LABEL_NAME_SAMPLE = "CategoryDialog.LABEL_NAME_SAMPLE";
	public static final String CAD_BUDGET_NAME = "CategoryDialog.LABEL_BUDGET";
	public static final String CAD_BUDGET_SAMPLE = "CategoryDialog.LABEL_BUDGET_SAMPLE";
	public static final String CAD_LABEL_NOTE = "CategoryDialog.LABEL_NOTE";
	public static final String CAD_LABEL_COLOR = "CategoryDialog.LABEL_COLOR";
	public static final String CAD_LABEL_NEW_CATEGORY = "CategoryDialog.LABEL_NEW_CATEGORY";
	public static final String CAD_LABEL_UPDATE_CATEGORY = "CategoryDialog.LABEL_UPDATE_CATEGORY";
	public static final String CAD_MESSAGE_USE_POSITIVE_AMOUNT = "CategoryDialog.MESSAGE_PLEASE_USE_POSITIVE_AMOUNT";

};