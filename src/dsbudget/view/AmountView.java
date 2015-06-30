package dsbudget.view;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;

public class AmountView extends DivRep {

	NumberFormat nf = NumberFormat.getCurrencyInstance();
	BigDecimal amount;
	
	public AmountView(DivRep parent, BigDecimal _amount) {
		super(parent);
		amount = _amount;
	}

	@Override
	protected void onEvent(DivRepEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setValue(BigDecimal _amount) {
		amount = _amount;
	}
	public void render(PrintWriter out) {
		String negative = "";
		if(amount.compareTo(BigDecimal.ZERO) < 0) {
			negative = "negative";
		}
		
		out.write("<span class=\""+negative+"\" id=\""+getNodeID()+"\">");
		out.write(StringEscapeUtils.escapeHtml(nf.format(amount)));
		out.write("</span>");
	}

}
