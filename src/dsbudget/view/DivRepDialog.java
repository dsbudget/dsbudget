package dsbudget.view;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;

abstract public class DivRepDialog extends DivRep {

	Boolean has_cancelbutton = false;
	Boolean rendered = false;
	
	public void setTitle(String title)
	{
		js("$(\"#"+getNodeID()+"_dialog\").dialog('option', 'title', \""+StringEscapeUtils.escapeHtml(title)+"\");");
	}

	public DivRepDialog(DivRep parent, Boolean _has_cancelbutton) {
		super(parent);
		has_cancelbutton = _has_cancelbutton;
	}
	public DivRepDialog(DivRep parent) {
		this(parent, false);
	}
	
	public void open() {
		js("$(\"#"+getNodeID()+"_dialog\").dialog('open');");
	}
	public void close() {
		js("$(\"#"+getNodeID()+"_dialog\").dialog('close');");
	}

	protected void onEvent(DivRepEvent e) {
		if(e.value.equals("cancel")) {
			onCancel();
		} else if(e.value.equals("submit")) {
			onSubmit();
		}
	}
	
	//dialog can't be redrawn it must be rendered once and only once.
	//make sure to render this to where it's never redrawn or it will misbehaves - mostly due to jQuery-UI limitation..
	public void redraw() {
		alert("DivRepDialog can't be redrawn.");
	}

	abstract public void onCancel();
	abstract public void onSubmit();
	
	//this will be called only once
	abstract public void renderDialog(PrintWriter out);
	
	public void render(PrintWriter out) {
		//dialog can be rendered only once
		if(!rendered) {
			out.write("<div id=\""+getNodeID()+"\">");
	
			out.write("<div class=\"divrep_hidden\" id=\""+getNodeID()+"_dialog\" title=\"untitled\">");
			renderDialog(out);
			out.write("</div>");
	
			out.write("<script type=\"text/javascript\">");
			out.write("$(\"#"+getNodeID()+"_dialog\").dialog({autoOpen: false, closeOnEscape: true, bgiframe: true, width: 'auto', height: 'auto', modal: true");
		
			out.write(",buttons: {");
			if(has_cancelbutton) {
				out.write("Cancel: function() {divrep('"+getNodeID()+"', null, 'cancel');},");
			}
			out.write("OK: function() {divrep('"+getNodeID()+"', null, 'submit');}}");
		
			out.write("});");	
			out.write("</script>");			

			out.write("</div>");
			rendered = true;
		}
	}

}
