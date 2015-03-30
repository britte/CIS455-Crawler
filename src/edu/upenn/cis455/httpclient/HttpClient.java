package edu.upenn.cis455.httpclient;

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
	
	public HttpClient() {}

	public HttpResponse getHead(String url, Date lastCrawled) throws IOException {
		try {
			// Try to establish a connection
			if (!connect(url)) return null;
			
			// Send head request
			sendHeadReq(lastCrawled);
			
			// Read in response
			HttpResponse res = readHeadResponse();
			
			// Close and clean client
			close();
			
			return res;
		} catch (Exception e){
			close();
			return null;
		}
	}
	
	public HttpResponse getResponse(String url, Date lastCrawled) throws IOException {
		try {
			// Try to establish a connection
			if (!connect(url)) return null;
			
			// Send get request
			sendGetReq(lastCrawled);
			
			// Read in response
			HttpResponse res = readFullResponse();
			
			// Close and clean client
			close();
			
			return res;
		} catch (Exception e){
			close();
			return null;
		}
	}
	
	// Given a url, try to establish a connection
	private boolean connect(String url) {
		try {
			urlObj = new URL(url);
			if (urlObj.getProtocol().equals("http")) {
				// HTTP connection 
				InetAddress addr = InetAddress.getByName(urlObj.getHost());
				this.s = new Socket(addr, 80);
				this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())));
				this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));	
				return true;
			} else if (urlObj.getProtocol().equals("https")){
				// HTTPS connection
				this.conn = (HttpsURLConnection) urlObj.openConnection();
				conn.setDoOutput(true);
				this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(conn.getOutputStream())));
				this.in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				return true;
			} else {
				// Invalid connection
				return false;
			}
			
		} catch (IOException e){
			System.out.println(e);
			return false;
		}
	}
	
	//
	// Http Request and Response Handling
	//
	
	private void sendHeadReq(Date lastCrawled) {
		String host = urlObj.getHost();
		String path = urlObj.getFile();
		out.println("HEAD " + path + " HTTP/1.1");
		out.println("Host: " + host);
		out.println("User-Agent: cis455crawler");
		out.println();
		out.flush();
	}
	
	private void sendGetReq(Date lastCrawled) {
		String host = urlObj.getHost();
		String path = urlObj.getFile();
		out.println("GET " + path + " HTTP/1.1");
		out.println("Host: " + host);
		out.println("User-Agent: cis455crawler");
		out.println();
		out.flush();
	}
	
	private HttpResponse readHeadResponse() throws IOException {
		try {
			
			int status;
			String contentType = null;
			Long contentLength = null;
			
			if (this.s != null) {
				// If reading through a Socket, header information is part of the input stream
				String line = in.readLine();
				if (line == null) return null;
				
				// Check the response status
				status = Integer.parseInt(line.split(" ")[1]);
				
				// Read through headers and check for important ones
				while (!line.isEmpty()) {
//					System.out.println(line);
					if (line.indexOf("Content-Length") != -1) {
						contentLength = Long.parseLong(line.substring(line.indexOf(":") + 1).trim());
					} else if (line.indexOf("Content-Type") != -1) {
						int end = line.indexOf(';');
						if (end != -1) {
							contentType = line.substring(line.indexOf(":") + 1, end).trim();
						} else {
							contentType = line.substring(line.indexOf(":") + 1).trim();
						}
					}
					line = in.readLine();
				}
				return new HttpResponse(this.urlObj, status, contentLength, contentType);
			} else {
				// If reading through a URLConnection, header information is part of the connection
				status = this.conn.getResponseCode();
				
				contentLength = this.conn.getContentLengthLong();
				
				contentType = this.conn.getContentType();
				int semi = contentType.indexOf(";");
				if (semi != -1) contentType = contentType.substring(0, semi);
				
				return new HttpResponse(this.urlObj, status, contentLength, contentType);
			}
		} catch (Exception e) {
			return null;
		} 
	}
	
	private HttpResponse readFullResponse() throws IOException {
		try {
			// Read through headers 
			HttpResponse res = readHeadResponse();
			
			// Don't read if status isn't 200
			if (res.getStatus() != 200) return null;
			
			// If the document is of a valid type read in the body
			if (res.isHtml()) { 
				StringBuilder body = new StringBuilder();
				long len = res.getDocLength();
				while (len > 0) {
					body.append((char) in.read());
					len --;
				}
				// Clean the body into XHTML 
				Tidy tidy = new Tidy();
				tidy.setMakeClean(true);
				tidy.setShowWarnings(false);
				tidy.setXHTML(true);
				res.setDocument(tidy.parseDOM(new ByteArrayInputStream(body.toString().getBytes()), null));
				return res;
			} else if (res.isXml()) {
				StringBuilder body = new StringBuilder();
				long len = res.getDocLength();
				while (len > 0) {
					body.append((char) in.read());
					len --;
				}

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = factory.newDocumentBuilder();
				res.setDocument(db.parse(new ByteArrayInputStream(body.toString().getBytes())));
				return res;
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
	
}
