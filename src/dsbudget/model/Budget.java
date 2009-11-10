package dsbudget.model;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

//Using XML - http://www.totheriver.com/learn/xml/xmltutorial.html#5.1

public class Budget implements XMLSerializer {
	public ArrayList<Page> pages = new ArrayList<Page>();
	public String openpage;
	
	public void fromXML(Element node) {
		openpage = node.getAttribute("openpage");
		
		//create pages
		NodeList nl = node.getChildNodes();
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				Element el = (Element)nl.item(i);
				if(el.getTagName().equals("Page")) {
					Element page_e = (Element)nl.item(i);
					if(page_e.getAttribute("name").equals("New Page")) continue;
					Page page = new Page(this);
					page.fromXML(page_e);
					pages.add(page);
				}
			}
		}
	}
	
	public static Budget loadXML(String xmlpath) {// "c:\tmp\BudgetDocument.xml"
		Budget budget = new Budget();
		
		//Load as DOM
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(xmlpath);
			NodeList roots = doc.getElementsByTagName("Budget");
			budget.fromXML((Element)roots.item(0));
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}

		return budget;
	}

	@Override
	public Element toXML() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Page findPage(Integer pageid) {	
		for(Page p : pages) {
			if(p.getID().equals(pageid)) {
				return p;
			}
		}
		return null;
	}
	
	
	public Page findPage(String pagename) {	
		for(Page p : pages) {
			if(p.name.equals(pagename)) {
				return p;
			}
		}
		return null;
	}
}
