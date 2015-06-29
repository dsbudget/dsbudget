package dsbudget.view;

import java.awt.Color;
import java.io.PrintWriter;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;

//This DivRep requires jQuery-UI
public class DivRepProgressbar extends DivRep {
	
	int value = 50;
	Color color = null;

	public DivRepProgressbar(DivRep parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}
	public void setValue(int value) { this.value = value; }
	public void setColor(Color _color) { color = _color; }

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		
		out.write("<div style=\"height: 18px;\" id=\""+getNodeID()+"_progressbar\"></div>");
		out.write("<script type=\"text/javascript\">");
		out.write("$(\"#"+getNodeID()+"_progressbar\").progressbar({value: "+value+"});");
		out.write("</script>");
		if(color != null) {
			out.write("<style type=\"text/css\">");
			out.write("#"+getNodeID()+"_progressbar .ui-widget-header {background: #"+String.format("%06x", (color.getRGB() & 0x00ffffff) )+"}");
			out.write("</style>");
		}
		out.write("</div>");
	}

}
