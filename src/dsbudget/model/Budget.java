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

import dsbudget.Main;
import dsbudget.SaveThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

//Using XML - http://www.totheriver.com/learn/xml/xmltutorial.html#5.1

public class Budget implements XMLSerializer {
	public ArrayList<Page> pages = new ArrayList<Page>();
	public String openpage;
	public static SaveThread savethread;
	
	public Budget()
	{
		savethread = new SaveThread(this);
		savethread.start();
	}
	
	public void save()
	{
		savethread.save();
	}
	
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
	
	public static Budget loadXML(File xmlpath, Budget budget) throws ParserConfigurationException, SAXException, IOException {
		
		//why am I re using budget? Because each servlet only initializes budet reference once
		//during init().. it's very hard to reset all of them
		if(budget == null) {
			budget = new Budget();
		} else {
			budget.pages.clear();
		}
		
		//Load as DOM
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(xmlpath);
		NodeList roots = doc.getElementsByTagName("Budget");
		budget.fromXML((Element)roots.item(0));

		return budget;
	}	
		
	public synchronized void saveXML(String xmlpath) throws ParserConfigurationException, IOException, TransformerException
	{	
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
	
		docBuilder = dbfac.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
	    DOMSource source = new DOMSource(toXML(doc));
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
	    transformer = tf.newTransformer();
		StreamResult result = new StreamResult(new OutputStreamWriter(new FileOutputStream(xmlpath), Charset.forName("UTF-8")));
		transformer.setOutputProperty(OutputKeys.INDENT,"no");
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		transformer.transform(source, result); 		
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
