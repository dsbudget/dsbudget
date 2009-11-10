package dsbudget.view;

import java.awt.Color;
import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepFormElement;

public class DivRepColorPicker extends DivRepFormElement<Color> {

	protected DivRepColorPicker(DivRep parent) {
		super(parent);
		value = Color.white;
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		value = new Color(Integer.parseInt(e.value));
	}

	//call this once to register
	static public void renderInit(PrintWriter out)
	{
		out.write("<style>");
		out.write(".divrep_colorpicker_red, .divrep_colorpicker_green, .divrep_colorpicker_blue {margin-left: 50px; width: 250px; clear: left; float: left; margin-bottom: 6px;}");
		out.write(".divrep_colorpicker_red .ui-slider-range { background: #f00; }");
		out.write(".divrep_colorpicker_green .ui-slider-range { background: #0f0; }");
		out.write(".divrep_colorpicker_blue .ui-slider-range { background: #00f; }");
		out.write(".divrep_colorpicker_color { width: 45px; height: 45px; }");
		out.write("</style>");
		out.write("<script type=\"text/javascript\">");
		out.write("function hexFromRGB (r, g, b) {	var hex = [r.toString(16),	g.toString(16),	b.toString(16)];$.each(hex, function (nr, val) {if (val.length == 1) {hex[nr] = '0' + val;}});return hex.join('').toUpperCase();}");		
		out.write("</script>");
	}
	
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		if(label != null) {
			out.write("<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
		}
		out.write("<div class=\"divrep_colorpicker_red\" id=\""+getNodeID()+"_red\"></div>");
		out.write("<div class=\"divrep_colorpicker_green\" id=\""+getNodeID()+"_green\"></div>");
		out.write("<div class=\"divrep_colorpicker_blue\" id=\""+getNodeID()+"_blue\"></div>");
		out.write("<div class=\"divrep_colorpicker_color ui-corner-all\" id=\""+getNodeID()+"_color\"></div>");
		
		out.write("<script type=\"text/javascript\">");
		out.write("function change_"+getNodeID()+"() {");
		out.write("var red = $(\"#"+getNodeID()+"_red\").slider('value');");
		out.write("var green = $(\"#"+getNodeID()+"_green\").slider('value');");
		out.write("var blue = $(\"#"+getNodeID()+"_blue\").slider('value');");
		out.write("divrep('"+getNodeID()+"', null, red*256*256+green*256+blue);");
		out.write("}");
		out.write("function refresh_"+getNodeID()+"() {");
		out.write("var red = $(\"#"+getNodeID()+"_red\").slider('value');");
		out.write("var green = $(\"#"+getNodeID()+"_green\").slider('value');");
		out.write("var blue = $(\"#"+getNodeID()+"_blue\").slider('value');");
		out.write("$(\"#"+getNodeID()+" .divrep_colorpicker_color\").css(\"background-color\", \"#\" + hexFromRGB(red, green, blue));");
		out.write("}");
		out.write("	$(\"#"+getNodeID()+"_red\").slider({change: change_"+getNodeID()+", slide: refresh_"+getNodeID()+", range: \"min\", max: 255, value: "+value.getRed()+"});");
		out.write("	$(\"#"+getNodeID()+"_green\").slider({change: change_"+getNodeID()+", slide: refresh_"+getNodeID()+", range: \"min\", max: 255, value: "+value.getGreen()+"});");
		out.write(" $(\"#"+getNodeID()+"_blue\").slider({change: change_"+getNodeID()+", slide: refresh_"+getNodeID()+", range: \"min\", max: 255, value: "+value.getBlue()+"});");
		out.write("refresh_"+getNodeID()+"();");
		out.write("</script>");
		out.write("</div>");
	}

}
