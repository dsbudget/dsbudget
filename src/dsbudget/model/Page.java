package dsbudget.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Page extends ObjectID implements XMLSerializer {
	Budget parent;
	
	//define static pages
	public static final Page search_page = new Page(null);
	static {
		search_page.name = "Search";
	}
	
	public String name;
	public Date created;
	public Boolean hide_budget;
	public Boolean hide_income;
	public Boolean hide_expense;
	public Boolean hide_balance;
	//public String previous_name;
	
	public ArrayList<Income> incomes = new ArrayList<Income>();
	public ArrayList<Category> categories = new ArrayList<Category>();
	
	public Boolean hasBalanceCircle(Page origin) {
		if(this == origin) return true;
		for(Income income : incomes) {
			if(income.hasBalanceCircle(origin)) {
				return true;
			}
		}
		return false;
	}
	
	public Page(Budget _parent) {
		super();
		parent = _parent;
		
		name = "Untitled";
		created = new Date();
		hide_budget = false;
		hide_income = false;
		hide_expense = false;
		hide_balance = false;
		//previous_name = null;
	}
	public Page clone() {
		Page page = new Page(parent);
		page.name = name;
		page.created = new Date();
		page.hide_budget = hide_budget;
		page.hide_income = hide_income;
		page.hide_expense = hide_expense;
		page.hide_balance = hide_balance;
		//page.previous_name = previous_name;
		
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
			total = total.add(category.getAmount());
		}
		return total;
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
	/*
	public Page getPreviousPage() {
		if(previous_name != null) {
			for(Page page : getParent().pages) {
				if(page == this) continue;//this should never happen, but..
				if(page.name.equals(previous_name)) {
					return page;
				}
			}	
		}
		return null;
	}
	*/
	
	public void fromXML(Element element) {

		name = element.getAttribute("name");
		created = new Date(Long.parseLong(element.getAttribute("ctime"))*1000L);
		
		if(element.hasAttribute("hide_budget")) {
			if(element.getAttribute("hide_budget").equals("yes")) {
				hide_budget = true;
			} else {
				hide_budget = false;
			}
		} else {
			hide_budget = false;
		}
		
		if(element.hasAttribute("hide_income")) {
			if(element.getAttribute("hide_income").equals("yes")) {
				hide_income = true;
			} else {
				hide_income = false;
			}
		} else {
			hide_income = false;
		}
		
		if(element.hasAttribute("hide_expense")) {
			if(element.getAttribute("hide_expense").equals("yes")) {
				hide_expense = true;
			} else {
				hide_expense = false;
			}
		} else {
			hide_expense = false;
		}
		
		if(element.hasAttribute("hide_balance")) {
			if(element.getAttribute("hide_balance").equals("yes")) {
				hide_balance = true;
			} else {
				hide_balance = false;
			}
		} else {
			hide_balance = false;
		}
		
		/*
		if(element.hasAttribute("previous_name")) {
			previous_name = element.getAttribute("previous_name");
		}
		*/
		
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
		elem.setAttribute("hide_budget", (hide_budget==true?"yes":"no"));
		elem.setAttribute("hide_income", (hide_income==true?"yes":"no"));
		elem.setAttribute("hide_expense", (hide_expense==true?"yes":"no"));
		elem.setAttribute("hide_balance", (hide_balance==true?"yes":"no"));
		/*
		if(previous_name != null) {
			elem.setAttribute("previous_name", previous_name);
		}
		*/
		for(Income income : incomes) {
			elem.appendChild(income.toXML(doc));
		}
		for(Category category : categories) {
			elem.appendChild(category.toXML(doc));
		}
		return elem;
	}

}
