package dsbudget.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XMLSerializer {
	void fromXML(Element element);
	Element toXML(Document doc);
}
