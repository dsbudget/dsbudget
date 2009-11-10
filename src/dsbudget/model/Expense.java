package dsbudget.model;

import java.math.BigDecimal;
import java.util.Date;

import org.w3c.dom.Element;

public class Expense implements XMLSerializer {
	public BigDecimal amount;
	public String description;
	public String where;
	public Date date;
	
	public void fromXML(Element element) {
		where = element.getAttribute("where");	
		description = element.getAttribute("desc");
		date = new Date(Long.parseLong(element.getAttribute("time"))*1000L);
		amount = Loader.loadAmount(element.getAttribute("amount"));
	}

	@Override
	public Element toXML() {
		// TODO Auto-generated method stub
		return null;
	}

}
