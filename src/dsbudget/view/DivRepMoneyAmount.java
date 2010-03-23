package dsbudget.view;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepTextBox;

public class DivRepMoneyAmount extends DivRepTextBox {
	static NumberFormat nf = NumberFormat.getCurrencyInstance();
	
	public DivRepMoneyAmount(DivRep parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	public void onEvent(DivRepEvent e) {
		String value = e.value.trim();
		setValue("");
		try {
			BigDecimal b = new BigDecimal(value);
			setValue(nf.format(b));
		} catch(NumberFormatException ne) {
			try {
				Number n = nf.parse(value);
				setValue(nf.format(n));
			} catch (ParseException e1) {
				//any other idea?
			}
		}
		redraw();
	}
	
	private static final long serialVersionUID = 1L;

}
