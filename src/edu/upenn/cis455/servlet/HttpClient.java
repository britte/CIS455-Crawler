package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

public class HttpClient {
	
	private Socket s; // only for http
	private HttpsURLConnection conn; // only for https
	private BufferedReader in;
	private PrintWriter out;
	private URL urlObj;
	
	private boolean valid = true;
	
	private int status;
	private Long docLength;
	private String docType;
	private Date lastModified;
	
	public HttpClient(String url) {		
		try {
			urlObj = new URL(url);
			if (urlObj.getProtocol().equals("http")) {
				// HTTP connection 
				InetAddress addr = InetAddress.getByName(urlObj.getHost());
				this.s = new Socket(addr, 80);
				this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())));
				this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));				
			} else if (urlObj.getProtocol().equals("https")){
				// HTTPS connection
				this.conn = (HttpsURLConnection) urlObj.openConnection();
				conn.setDoOutput(true);
				this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(conn.getOutputStream())));
				this.in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			} else {
				// Invalid connection
				valid = false; 
				return;
			}
			
			// After establishing connection, send HEAD request
			sendHeadReq();
			readHead();
			
		} catch (IOException e){
			System.out.println(e);
			valid = false;
		}
	}
	
	//
	// Http Request and Response Handling
	//
	
	private void sendHeadReq() {
		String host = urlObj.getHost();
		String path = urlObj.getFile();
		out.println("HEAD " + path + " HTTP/1.1");
		out.println("Host: " + host);
		out.println("User-Agent: cis455crawler");
		out.println();
		out.flush();
	}
	
	private void sendGetReq() {
		String host = urlObj.getHost();
		String path = urlObj.getFile();
		out.println("GET " + path + " HTTP/1.1");
		out.println("Host: " + host);
		out.println("User-Agent: cis455crawler");
		out.println();
		out.flush();
	}
	
	private void readHead() {
		if (!valid) return; // Don't read if connection failed
		try {
			if (this.s != null) {
				// If reading through a Socket, header information is part of the input stream
				String line = in.readLine();
				if (line == null) return;
				
				// Check the response status
				this.status = Integer.parseInt(line.split(" ")[1]);
				
				// Read through headers and check for important ones
				while (!line.isEmpty()) {
//					System.out.println(line);
					if (line.indexOf("Content-Length") != -1) {
						this.docLength = Long.parseLong(line.substring(line.indexOf(":") + 1).trim());
					} else if (line.indexOf("Content-Type") != -1) {
						int end = line.indexOf(';');
						if (end != -1) {
							this.docType = line.substring(line.indexOf(":") + 1, end).trim();
						} else {
							this.docType = line.substring(line.indexOf(":") + 1).trim();
						}
					} else if (line.indexOf("Last-Modified") != -1) {
						SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
						this.lastModified = format.parse(line.substring(line.indexOf(":") + 1).trim());
					}
					line = in.readLine();
				}
			} else {
				// If reading through a URLConnection, header information is part of the connection
				this.status = this.conn.getResponseCode();
				
				this.docLength = this.conn.getContentLengthLong();
				
				this.docType = this.conn.getContentType();
				int semi = this.docType.indexOf(";");
				if (semi != -1) this.docType = this.docType.substring(0, semi);
				
				this.lastModified = new Date(this.conn.getLastModified());
			}
		} catch (Exception e) {
			return;
		}
	}
	
	public Document getDoc() {
		if (!valid) return null; // Don't read if connection failed
		try {
			
			// Read through headers
			sendGetReq();
			readHead();
			
			// Don't read if status isn't 200
			if (this.status != 200) return null;
			
			// If the document is of a valid type read in the body
			if (isHtml()) { 
				StringBuilder body = new StringBuilder();
				long len = docLength;
				while (len > 0) {
					body.append((char) in.read());
					len --;
				}
				// Clean the body into XHTML 
				Tidy tidy = new Tidy();
				tidy.setMakeClean(true);
				tidy.setShowWarnings(false);
				tidy.setXHTML(true);
				return tidy.parseDOM(new ByteArrayInputStream(body.toString().getBytes()), null);
			} else if (isXml()) {
				StringBuilder body = new StringBuilder();
				long len = docLength;
				while (len > 0) {
					body.append((char) in.read());
					len --;
				}

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = factory.newDocumentBuilder();
				return db.parse(new ByteArrayInputStream(body.toString().getBytes()));
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	public void close() throws IOException {
		this.out.close();
		this.in.close();
		if (this.s != null) this.s.close();
		if (this.conn != null) this.conn.disconnect();
	}
	
	//
	// Helpers to determine state of the document
	//
	
	public boolean isValid() {
		return this.valid;
	}
	
	public boolean isHtml() {
		return docType != null && docType.equals("text/html");
	}
	
	public boolean isXml() {
		return docType != null && (docType.equals("text/xml") ||
								   docType.equals("application/xml") ||
								   docType.endsWith("+xml"));
	}

	public boolean modifiedSince(Date d) {
		return this.lastModified == null || this.lastModified.after(d);
	}
	
	// Return the base url for the page
	public String getBaseUrl(Document d) {
		if (!this.isValid()) return null;
		NodeList base = d.getElementsByTagName("base");
		if (base != null && base.getLength() > 0 && base.item(0).getNodeType() == Node.ELEMENT_NODE) {
			// If the document contains a base element return the referenced base
			Element baseElem = (Element) base.item(0);
			return baseElem.getAttribute("href");
		} else {
			// If there is no base element the base is the directory of the current page
			String protocol = this.urlObj.getProtocol() + "://";
			String authority = this.urlObj.getAuthority();
			String path = this.urlObj.getPath();
			int lastSlash = path.lastIndexOf('/');
			if (lastSlash != -1) path = path.substring(0, lastSlash + 1);
			return protocol + authority + path;
		}
	}
	
	// Return the root url for the page
	public String getRootUrl() {
		return this.urlObj.getProtocol() + "://" + this.urlObj.getAuthority() + "/";
	}
	
	public long getDocLength() {
		return this.docLength;
	}
}
