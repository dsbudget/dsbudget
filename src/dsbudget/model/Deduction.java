package dsbudget.model;

import java.math.BigDecimal;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Deduction implements XMLSerializer {

	public BigDecimal amount;
	public String description;
	
	public Deduction clone()
	{
		Deduction deduction = new Deduction();
		deduction.amount = amount;
		deduction.description = description;
		return deduction;
	}
	
	public void fromXML(Element element) {
		amount = Loader.loadAmount(element.getAttribute("amount"));
		description = element.getAttribute("desc");
	}

	public Element toXML(Document doc) {
		Element elem = doc.createElement("Deduction");
		elem.setAttribute("amount", Loader.saveAmount(amount).toString());
		elem.setAttribute("desc", description);
		return elem;
	}

}
