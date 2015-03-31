package edu.upenn.cis455.httpclient;

import java.io.ByteArrayInputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

public class HttpResponse {
	
	private URL url;
	
	private int status;
	private Long docLength;
	private String docType;
	
	private String dString;
	
	public HttpResponse(URL url, int status, Long len, String type) {	
		this.url = url;
		this.status = status;
		this.docLength = len;
		this.docType = type;
	}
	
	//
	// Helpers to determine state of the document
	//
	
	public boolean isHtml() {
		return docType != null && docType.equals("text/html");
	}
	
	public boolean isXml() {
		return docType != null && (docType.equals("text/xml") ||
								   docType.equals("application/xml") ||
								   docType.endsWith("+xml"));
	}
	
	public static boolean isHtml(String docType) {
		return docType != null && docType.equals("text/html");
	}
	
	public static boolean isXml(String docType) {
		return docType != null && (docType.equals("text/xml") ||
								   docType.equals("application/xml") ||
								   docType.endsWith("+xml"));
	}

	public long getDocLength() {
		return this.docLength;
	}
	
	public String getDocType() {
		return this.docType;
	}
	
	public int getStatus() {
		return this.status;
	}
	
	public String getDocString() {
		return this.dString;
	}
	
	public static Document toDoc(String s, String docType) {
		try {
			if (isHtml(docType)) {
				Tidy tidy = new Tidy();
				tidy.setMakeClean(true);
				tidy.setShowWarnings(false);
				tidy.setXHTML(true);
				return tidy.parseDOM(new ByteArrayInputStream(s.getBytes()), null);
			} else if (isXml(docType)){
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = factory.newDocumentBuilder();
				return db.parse(new ByteArrayInputStream(s.getBytes()));
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	public Document getDoc() {
		return toDoc(this.dString, this.docType);
	}
	
	// Return the base url for the page
	public String getBaseUrl() {
		if (this.dString != null) {
			Document d = getDoc();
			if (d == null) return null;
			NodeList base = d.getElementsByTagName("base");
			if (base != null && base.getLength() > 0 && base.item(0).getNodeType() == Node.ELEMENT_NODE) {
				// If the document contains a base element return the referenced base
				Element baseElem = (Element) base.item(0);
				return baseElem.getAttribute("href");
			} else {
				// If there is no base element the base is the directory of the current page
				String protocol = this.url.getProtocol() + "://";
				String authority = this.url.getAuthority();
				String path = this.url.getPath();
				int lastSlash = path.lastIndexOf('/');
				if (lastSlash != -1) path = path.substring(0, lastSlash + 1);
				return protocol + authority + path;
			}
		} else {
			String protocol = this.url.getProtocol() + "://";
			String authority = this.url.getAuthority();
			String path = this.url.getPath();
			int lastSlash = path.lastIndexOf('/');
			if (lastSlash != -1) path = path.substring(0, lastSlash + 1);
			return protocol + authority + path;
		}
		
	}
	
	// Return the root url for the page
	public String getRootUrl() {
		return this.url.getProtocol() + "://" + this.url.getAuthority() + "/";
	}
	
	public void setDocument (String dString){
		this.dString = dString;
	}
}
