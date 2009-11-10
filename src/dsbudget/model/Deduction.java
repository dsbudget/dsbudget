package dsbudget.model;

import java.math.BigDecimal;

import org.w3c.dom.Element;

public class Deduction implements XMLSerializer {

	public BigDecimal amount;
	public String description;
	
	public void fromXML(Element element) {
		amount = Loader.loadAmount(element.getAttribute("amount"));
		description = element.getAttribute("desc");
	}

	@Override
	public Element toXML() {
		// TODO Auto-generated method stub
		return null;
	}

}
