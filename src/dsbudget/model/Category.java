package dsbudget.model;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Category extends ObjectID implements XMLSerializer {

	private Page parent;
	
	public BigDecimal amount;
	public Color color;
	public String description;
	
	public Boolean fixed;
	public Boolean hide_graph;
	public String name;
	public Boolean auto_adjust;
	
	public ArrayList<Expense> expenses = new ArrayList<Expense>();
	
	public Category clone(Page newparent)
	{
		Category category = new Category(newparent);
		category.amount = amount;
		category.color = color;
		category.description = description;
		category.fixed = fixed;
		category.hide_graph = hide_graph;
		category.name = name;
		category.auto_adjust = auto_adjust;
		
		category.expenses = new ArrayList<Expense>();
		for(Expense expense : expenses) {
			category.expenses.add(expense.clone());
		}
		return category;
	}
	
	public ArrayList<Expense> getExpensesSortByDate()
	{
		  Collections.sort(expenses, new Comparator(){
	            public int compare(Object o1, Object o2) {
	            	Expense p1 = (Expense) o1;
	            	Expense p2 = (Expense) o2;
	               return p1.date.compareTo(p2.date);
	            }
	      });
		  return expenses;
	}
	
	public void removeExpense(Expense e)
	{
		expenses.remove(e);
	}
	
	public void addExpense(Expense e)
	{
		expenses.add(e);
	}
	
	public Category(Page _parent)
	{
		parent = _parent;
	}
	
	public BigDecimal getTotalExpense()
	{
		BigDecimal total = new BigDecimal(0);
		for(Expense expense : expenses) {
			total = total.add(expense.amount);
		}
		return total;
	}
	
	public void fromXML(Element element) {
		amount = Loader.loadAmount(element.getAttribute("budget"));
		String color_str = element.getAttribute("color");
		long color_comp = Long.parseLong(color_str);
		int r = (int)((color_comp>>0)&0xff);
		int g = (int)((color_comp>>8)&0xff);
		int b = (int)((color_comp>>16)&0xff);
		color = new Color(r,g,b);
		description = element.getAttribute("desc");
		if(element.getAttribute("fixed").equals("yes")) {
			fixed = true;
		} else {
			fixed = false;
		}
		if(element.getAttribute("hide_graph").equals("yes")) {
			hide_graph = true;
		} else {
			hide_graph = false;
		}
		if(element.getAttribute("auto_adjust").equals("yes")) {
			auto_adjust = true;
		} else {
			auto_adjust = false;
		}
		name = element.getAttribute("name");
		
		//expense
		NodeList nl = element.getChildNodes();
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				Element el = (Element)nl.item(i);
				if(el.getTagName().equals("Spent")) {
					Expense expense = new Expense();
					expense.fromXML(el);
					expenses.add(expense);
				}
			}
		}
	}
/*
	public BigDecimal amount;
	public Color color;
	public String description;
	
	public Boolean fixed;
	public Boolean hide_graph;
	public String name;
	
	public ArrayList<Expense> expenses = new ArrayList<Expense>();
 */
	public Element toXML(Document doc) {
		Element elem = doc.createElement("Category");
		elem.setAttribute("budget", Loader.saveAmount(amount).toString());
		
		long c = color.getRed();
		c |= ((long)color.getGreen() << 8);
		c |= ((long)color.getBlue() << 16);
		
		elem.setAttribute("color", String.valueOf(c));
		elem.setAttribute("desc", description);
		elem.setAttribute("fixed", (fixed==true?"yes":"no"));
		elem.setAttribute("hide_graph", (hide_graph==true?"yes":"no"));
		elem.setAttribute("name", name);
		elem.setAttribute("auto_adjust", (auto_adjust==true?"yes":"no"));
		for(Expense expense : expenses) {
			elem.appendChild(expense.toXML(doc));
		}
		return elem;
	}

}
