package dsbudget.model;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileWriter;
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
	
	public static Budget loadXML(String xmlpath) {
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
	public void saveXML(String xmlpath)
	{
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer serializer;
		try {
			docBuilder = dbfac.newDocumentBuilder();
	        Document doc = docBuilder.newDocument();
		    DOMSource source = new DOMSource(toXML(doc));
			
			serializer = tf.newTransformer();
			StreamResult result = new StreamResult(new FileWriter(xmlpath));
			serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT,"no");
			serializer.transform(source, result); 
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Element toXML(Document doc) {
		Element elem = doc.createElement("Budget");
		elem.setAttribute("docversion", "2.00");
		elem.setAttribute("openpage", openpage);
		for(Page page : pages) {
			elem.appendChild(page.toXML(doc));
		}
		
		//add "New Page" for backward compatibility
		Element newpage = doc.createElement("Page");
		newpage.setAttribute("name", "New Page");
		newpage.setAttribute("ctime", "0");
		elem.appendChild(newpage);
		
		return elem;
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
