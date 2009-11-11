package dsbudget.model;

import java.math.BigDecimal;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Expense implements XMLSerializer {
	public BigDecimal amount;
	public String description;
	public String where;
	public Date date;
	
	public Expense clone()
	{
		Expense expense = new Expense();
		expense.amount = amount;
		expense.description = description;
		expense.where = where;
		expense.date = (Date) date.clone();
		return expense;
	}
	
	public void fromXML(Element element) {
		where = element.getAttribute("where");	
		description = element.getAttribute("desc");
		date = new Date(Long.parseLong(element.getAttribute("time"))*1000L);
		amount = Loader.loadAmount(element.getAttribute("amount"));
	}

	public Element toXML(Document doc) {
		Element elem = doc.createElement("Spent");
		elem.setAttribute("amount", Loader.saveAmount(amount).toString());
		elem.setAttribute("desc", description);
		elem.setAttribute("where", where);
		elem.setAttribute("time", String.valueOf(date.getTime()/1000L));
		return elem;
	}

}
