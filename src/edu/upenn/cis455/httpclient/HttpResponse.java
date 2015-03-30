package edu.upenn.cis455.httpclient;

import java.net.URL;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HttpResponse {
	
	private URL url;
	
	private int status;
	private Long docLength;
	private String docType;
	
	private Document d;
	
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

	public long getDocLength() {
		return this.docLength;
	}
	
	public String getDocType() {
		return this.docType;
	}
	
	public int getStatus() {
		return this.status;
	}
	
	public Document getDoc() {
		return this.d;
	}
	
	// Return the base url for the page
	public String getBaseUrl() {
		if (this.d != null) {
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
	
	public void setDocument (Document d){
		this.d = d;
	}
}
