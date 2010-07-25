package dsbudget.model;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dsbudget.servlet.ServletBase;

public class Category extends ObjectID implements XMLSerializer {
	static Logger logger = Logger.getLogger(Category.class);
	
	private Page parent;
	
	public BigDecimal amount;
	public Color color;
	public String description;
	
	public Boolean fixed;
	public Boolean hide_balance_graph;
	public Boolean hide_pie_graph;
	public String name;
	
	public enum SortBy {WHERE, DATE, AMOUNT};
	public SortBy sort_by;
	public Boolean sort_reverse;
	
	//public enum GraphType {BALANCE, PIE}
	//public GraphType graph_type;
	
	public ArrayList<Expense> expenses = new ArrayList<Expense>();
	
	public Category clone(Page newparent)
	{
		Category category = new Category(newparent);
		category.amount = amount;
		category.color = color;
		category.description = description;
		category.fixed = fixed;
		category.hide_balance_graph = hide_balance_graph;
		category.hide_pie_graph = hide_pie_graph;
		category.name = name;
		category.sort_by = sort_by;
		category.sort_reverse = sort_reverse;
		//category.graph_type = graph_type;
		
		category.expenses = new ArrayList<Expense>();
		for(Expense expense : expenses) {
			category.expenses.add(expense.clone());
		}
		return category;
	}
	
	public ArrayList<Expense> getExpenses()
	{
		  return expenses;
	}
	
	public ArrayList<Expense> getExpensesSortedBy(final SortBy by, final boolean reverse) {
		  Collections.sort(expenses, new Comparator<Expense>(){
	            public int compare(Expense o1, Expense o2) {
	            	Expense p1, p2;
	            	if(reverse) {
	            		p1 = o2;
	            		p2 = o1;
	            	} else {
	            		p1 = o1;
	            		p2 = o2;
	            	}
	            	
	            	switch(by) {
	            	case AMOUNT:
	            		return p1.amount.compareTo(p2.amount);
	            	case DATE:
	            		return p1.date.compareTo(p2.date);
	            	case WHERE:
	            		return p1.where.toLowerCase().compareTo(p2.where.toLowerCase());
	            	}
	            	return 0;
	            }
	      });
		  return expenses;	
	}
	
	public ArrayList<Expense> getExpensesSorted() {
		return getExpensesSortedBy(sort_by, sort_reverse);
	}
	/*
	public ArrayList<Expense> getExpensesSortByDate()
	{
		  Collections.sort(expenses, new Comparator<Expense>(){
	            public int compare(Expense o1, Expense o2) {
	            	Expense p1 = o1;
	            	Expense p2 = o2;
	               return p1.date.compareTo(p2.date);
	            }
	      });
		  return expenses;
	}
	*/
	
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
			if(expense.tentative) continue;
			total = total.add(expense.amount);
		}
		return total;
	}
	public BigDecimal getTotalScheduled()
	{
		BigDecimal total = new BigDecimal(0);
		for(Expense expense : expenses) {
			if(expense.tentative) {
				total = total.add(expense.amount);
			}
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

		hide_balance_graph = false;
		if(element.hasAttribute("hide_graph")) {
			//depricated value
			if(element.getAttribute("hide_graph").equals("yes")) {
				hide_balance_graph = true;
			} 
		}
		if(element.hasAttribute("hide_balance_graph")) {
			if(element.getAttribute("hide_balance_graph").equals("yes")) {
				hide_balance_graph = true;
			} 
		}
		
		hide_pie_graph = true;
		if(element.hasAttribute("hide_pie_graph")) {
			if(element.getAttribute("hide_pie_graph").equals("no")) {
				hide_pie_graph = false;
			}
		}
		
		name = element.getAttribute("name");

		sort_by = SortBy.DATE;
		if(element.hasAttribute("sort_by")) {
			try {
				sort_by = SortBy.valueOf(element.getAttribute("sort_by"));
			} catch(Exception e) {
				logger.warn("invalid sort_by parameter on category - ignoring and using default", e);
			}
		}
		
		if(element.hasAttribute("sort_reverse")) {
	 		if(element.getAttribute("sort_reverse").equals("yes")) {
				sort_reverse = true;
			} else {
				sort_reverse = false;
			}
		} else {
			sort_reverse = false;
		}
		/*
		if(element.hasAttribute("graph_type")) {
			graph_type = GraphType.valueOf(element.getAttribute("graph_type"));
		} else {
			graph_type = GraphType.BALANCE;
		}
		*/
		
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

	public Element toXML(Document doc) {
		Element elem = doc.createElement("Category");
		elem.setAttribute("budget", Loader.saveAmount(amount).toString());
		
		long c = color.getRed();
		c |= ((long)color.getGreen() << 8);
		c |= ((long)color.getBlue() << 16);
		
		elem.setAttribute("color", String.valueOf(c));
		elem.setAttribute("desc", description);
		elem.setAttribute("fixed", (fixed==true?"yes":"no"));
		elem.setAttribute("hide_balance_graph", (hide_balance_graph==true?"yes":"no"));
		elem.setAttribute("hide_pie_graph", (hide_pie_graph==true?"yes":"no"));
		elem.setAttribute("name", name);
		elem.setAttribute("sort_by", sort_by.toString());
		elem.setAttribute("sort_reverse", (sort_reverse==true?"yes":"no"));
		//elem.setAttribute("graph_type", graph_type.toString());
		
		for(Expense expense : expenses) {
			elem.appendChild(expense.toXML(doc));
		}
		return elem;
	}

}
