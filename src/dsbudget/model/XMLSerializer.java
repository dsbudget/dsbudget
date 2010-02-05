package dsbudget.model;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XMLSerializer extends Serializable {
	void fromXML(Element element);
	Element toXML(Document doc);
}
