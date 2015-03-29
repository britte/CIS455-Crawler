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
import java.net.UnknownHostException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

public class HttpClient {
	
	private Socket s;
	private BufferedReader in;
	private PrintWriter out;
	
	private boolean valid = true;
	
	public HttpClient(String url) {		
		try {
			URL urlObj = new URL(url);
			InetAddress addr = InetAddress.getByName(urlObj.getHost());
			this.s = new Socket(addr, 80);
			this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())));
			this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			sendReq(urlObj.getHost(), urlObj.getFile());
		} catch (IOException e){
			valid = false;
		}
	}
	
	public boolean isValid() {
		return this.valid;
	}
	
	public Document getDoc() {
		// If an error occured trying to connect to the url, 
		// no document can be created so return null
		if (!valid) return null;
		try {
			String line = in.readLine();
			if (line == null) return null;
			
			// Check for a successful response
			if (line.indexOf("200") == -1) return null; 
			
			// Read through headers and check for content length and type
			Integer len = null;
			String type = null;
			while (!line.isEmpty()) {
	//			System.out.println(line);
				if (line.indexOf("Content-Length") != -1) {
					len = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
				} else if (line.indexOf("Content-Type") != -1) {
					type = line.substring(line.indexOf(":") + 1).trim();
				}
				line = in.readLine();
			}
			
			// If the document is of a valid type read in the body
			if (len == null || type == null) return null;
			if (type.equals("text/html")) {
				StringBuilder body = new StringBuilder();
				while (len > 0) {
					body.append((char) in.read());
					len --;
				}
				// Clean the body into XHTML 
				Tidy tidy = new Tidy();
				tidy.setMakeClean(true);
				tidy.setXHTML(true);
				return tidy.parseDOM(new ByteArrayInputStream(body.toString().getBytes()), null);
			} else if (type.equals("text/xml") ||
				type.equals("application/xml") ||
				type.endsWith("+xml")) {
				StringBuilder body = new StringBuilder();
				while (len > 0) {
					body.append((char) in.read());
					len --;
				}
				// Clean the body into XHTML 
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
	
	private void sendReq(String host, String path) {
//		System.out.println(host + " " + path);
		out.println("GET " + path + " HTTP/1.1");
		out.println("Host: " + host);
		out.println();
		out.flush();
	}
	
	public void close() throws IOException {
		this.out.close();
		this.in.close();
		this.s.close();
	}

}
