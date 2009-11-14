package dsbudget.model;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Income implements XMLSerializer {
	private String __balance_from_name; //used only temporarily to load the balance_from lateron
	
	public Page balance_from = null;
	public BigDecimal amount;
	public String description;
	public ArrayList<Deduction> deductions = new ArrayList<Deduction>();
	
	Page parent;
	
	public Income(Page _parent)
	{
		parent = _parent;
	}
	public Page getParent() { return parent; }
	
	public Income clone(Page newparent)
	{
		Income income = new Income(newparent);
		//income.balance_from_name = balance_from_name;
		income.balance_from = balance_from;
		income.amount = amount;
		income.description = description;
		income.deductions = new ArrayList<Deduction>();
		for(Deduction deduction : deductions) {
			income.deductions.add(deduction.clone());
		}
		
		return income;
	}
	
	public BigDecimal getAmount()
	{
		if(__balance_from_name != null) {
			balance_from =  parent.getParent().findPage(__balance_from_name);
			__balance_from_name = null;//we don't need this anymore..
		}
		
		if(balance_from == null) {
			return amount;	
		} else {
			return balance_from.getBalance();
		}
	}
	
	public Boolean hasBalanceCircle(Page origin) 
	{
		if(balance_from == null) {
			return false;
		}
		return balance_from.hasBalanceCircle(origin);
	}
	
	public String getName() {
		String name = description;
		if(balance_from != null) {
			name = "Balance from " + StringEscapeUtils.escapeHtml(balance_from.name);
		}
		return name;
	}
	
	public BigDecimal getTotalDeduction() {
		BigDecimal total = new BigDecimal(0);
		for(Deduction deduction : deductions) {
			total = total.add(deduction.amount);
		}
		return total;
	}

	public void fromXML(Element element) 
	{
		if(element.getAttribute("balance").equals("yes")) {
			__balance_from_name = element.getAttribute("balance_from");
			//balance_from will be set later (when requested)
		} else {
			amount = Loader.loadAmount(element.getAttribute("amount"));
			__balance_from_name = null;
		}

		description = element.getAttribute("desc");
		
		//deduction
		NodeList nl = element.getChildNodes();
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				Element el = (Element)nl.item(i);
				if(el.getTagName().equals("Deduction")) {
					Deduction deduction = new Deduction();
					deduction.fromXML(el);
					deductions.add(deduction);
				}
			}
		}
	}

	public Element toXML(Document doc) {
		Element elem = doc.createElement("Income");
		if(balance_from == null) {
			elem.setAttribute("balance", "no");
			elem.setAttribute("amount", Loader.saveAmount(getAmount()).toString());
		} else {
			elem.setAttribute("balance", "yes");
			elem.setAttribute("balance_from", balance_from.name);
		}
		elem.setAttribute("desc", description);
		for(Deduction deduction : deductions) {
			elem.appendChild(deduction.toXML(doc));
		}
		return elem;
	}

}
