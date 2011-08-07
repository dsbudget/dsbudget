package dsbudget.model;

import java.math.BigDecimal;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Deduction implements XMLSerializer {

	private BigDecimal amount_or_percentage;
	private Boolean amount_is_percentage;
	public BigDecimal getAmount(BigDecimal total_income) {
		if(amount_is_percentage) {
			return total_income.multiply(amount_or_percentage);
		}
		return amount_or_percentage;
	}
	public Boolean isPercentage() {
		return amount_is_percentage;
	}
	public void setAmount(BigDecimal amount) {
		this.amount_or_percentage = amount;
		this.amount_is_percentage = false;
	}
	public void setAmountAsPercentage(BigDecimal amount) {
		this.amount_or_percentage = amount;
		this.amount_is_percentage = true;
	}
	public BigDecimal getPercentage() {
		if(amount_is_percentage) {
			return amount_or_percentage;
		}
		return null;
	}
	
	public String description;
	
	
	public Deduction clone()
	{
		Deduction deduction = new Deduction();
		deduction.amount_or_percentage = amount_or_percentage;
		deduction.amount_is_percentage = amount_is_percentage;
		deduction.description = description;
		return deduction;
	}
	
	public void fromXML(Element element) {
		amount_or_percentage = Loader.loadAmount(element.getAttribute("amount"));
		amount_is_percentage = false;
		if(element.hasAttribute("amount_is_percentage")) {
			if(element.getAttribute("amount_is_percentage").equals("true")) {
				amount_is_percentage = true;
			}
		}
		description = element.getAttribute("desc");
	}

	public Element toXML(Document doc) {
		Element elem = doc.createElement("Deduction");
		elem.setAttribute("amount", Loader.saveAmount(amount_or_percentage).toString());
		elem.setAttribute("amount_is_percentage", (amount_is_percentage==true?"true":"false"));
		elem.setAttribute("desc", description);
		return elem;
	}

}
