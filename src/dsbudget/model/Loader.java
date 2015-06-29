package dsbudget.model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;

public class Loader {
	static BigDecimal loadAmount(String amount_str)
	{
		BigDecimal amount =  new BigDecimal(amount_str);
		Currency currency = NumberFormat.getCurrencyInstance().getCurrency();
		BigDecimal divider = new BigDecimal(10);
		divider = divider.pow(currency.getDefaultFractionDigits());
		amount = amount.divide(divider);
		return amount;
	}
	static Long saveAmount(BigDecimal amount)
	{
		Currency currency = NumberFormat.getCurrencyInstance().getCurrency();
		BigDecimal multiplier = new BigDecimal(10);
		multiplier = multiplier.pow(currency.getDefaultFractionDigits());
		amount = amount.multiply(multiplier);
		return amount.longValue();
	}
}
