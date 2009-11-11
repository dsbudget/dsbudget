package dsbudget.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Page extends ObjectID implements XMLSerializer {
	Budget parent;
	
	public String name;
	public Date created;
	//public Boolean b100dist;
	
	public ArrayList<Income> incomes = new ArrayList<Income>();
	public ArrayList<Category> categories = new ArrayList<Category>();
	
	public Page(Budget _parent) {
		super();
		parent = _parent;
	}
	public Page clone() {
		Page page = new Page(parent);
		page.name = name;
		page.created = new Date();
		
		page.incomes = new ArrayList<Income>();
		for(Income income : incomes) {
			page.incomes.add(income.clone(page));
		}
		
		page.categories = new ArrayList<Category>();
		for(Category category : categories) {
			page.categories.add(category.clone(page));
		}

		return page;
	}
	
	public Budget getParent() { return parent; }
	
	public BigDecimal getTotalIncome() {
		BigDecimal total = new BigDecimal(0);;
		for(Income income : incomes) {
			total = total.add(income.getAmount());
		}
		return total;
	}
	public void removeCategory(Category category) {
		categories.remove(category);
	}
	
	public BigDecimal getTotalIncomeDeduction() {
		BigDecimal total = new BigDecimal(0);
		for(Income income : incomes) {
			total = total.add(income.getTotalDeduction());
		}
		return total;
	}
	public BigDecimal getTotalBudgetted()
	{
		BigDecimal total = new BigDecimal(0);
		for(Category category : categories) {
			total = total.add(category.amount);
		}
		return total;
	}
	
	public void fromXML(Element element) {
		/*
		if(element.getAttribute("b100dist").equals("yes")) {
			b100dist = true;
		} else {
			b100dist = false;
		}
		*/
		name = element.getAttribute("name");
		created = new Date(Long.parseLong(element.getAttribute("ctime"))*1000L);
		
		//income / category
		NodeList nl = element.getChildNodes();
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				Element el = (Element)nl.item(i);
				if(el.getTagName().equals("Income")) {
					Income income = new Income(this);
					income.fromXML(el);
					incomes.add(income);
				} else if(el.getTagName().equals("Category")) {
					Category cat = new Category(this);
					cat.fromXML(el);
					categories.add(cat);	
				}
			}
		}
	}

	public Element toXML(Document doc) {
		Element elem = doc.createElement("Page");
		elem.setAttribute("name", name);
		elem.setAttribute("ctime", String.valueOf(created.getTime()/1000L));
		for(Income income : incomes) {
			elem.appendChild(income.toXML(doc));
		}
		for(Category category : categories) {
			elem.appendChild(category.toXML(doc));
		}
		return elem;
	}
	
	public BigDecimal getBalance()
	{
		BigDecimal balance = getTotalIncome();
		balance = balance.subtract(getTotalIncomeDeduction());
		for(Category category : categories) {
			balance = balance.subtract(category.getTotalExpense());
		}
		return balance;
	}
	public Category findCategory(Integer catid) {	
		for(Category cat : categories) {
			if(cat.getID().equals(catid)) {
				return cat;
			}
		}
		return null;
	}
}
