package edu.upenn.cis455.httpclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class HttpClient {
	
	private String dateFormat = "EEE, d MMM YYYY HH:mm:ss z";
	
	private Socket s; // only for http
	private HttpsURLConnection conn; // only for https
	private BufferedReader in;
	private PrintWriter out;
	
	private URL urlObj;
	
	public HttpClient() {}

	public HttpResponse getHead(String url, Date lastCrawled) throws IOException {
		try {
			urlObj = new URL(url);
			if (urlObj.getProtocol().equals("http")) {
				// Create a socket connection
				connectHttp(urlObj.getHost());
				sendHeadReq(lastCrawled);
				
				// Read in response
				HttpResponse res = readHeadResponse();
				close();
				return res;
			} else if (urlObj.getProtocol().equals("https")){
				// Create a URLConnection
				this.conn = (HttpsURLConnection) urlObj.openConnection();
				this.conn.setRequestMethod("HEAD");
				this.conn.setRequestProperty("Host", urlObj.getHost());
				this.conn.setRequestProperty("User-Agent", "cis455crawler");
				if (lastCrawled != null) {
					this.conn.setIfModifiedSince(lastCrawled.getTime());
				}
				
				// Read in response
				this.in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				HttpResponse res = readHeadResponse();
				close();
				return res;
			} else {
				// Invalid connection
				return null;
			}
			
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public HttpResponse getResponse(String url) throws IOException {
		try {
			urlObj = new URL(url);
			if (urlObj.getProtocol().equals("http")) {
				// Create a socket connection
				connectHttp(urlObj.getHost());
				sendGetReq();
				
				// Read in response
				HttpResponse res = readFullResponse();
				close();
				return res;
			} else if (urlObj.getProtocol().equals("https")){
				// Create a URLConnection
				this.conn = (HttpsURLConnection) urlObj.openConnection();
				this.conn.setRequestMethod("GET");
				this.conn.setRequestProperty("Host", urlObj.getHost());
				this.conn.setRequestProperty("User-Agent", "cis455crawler");
				
				// Read in response
				this.in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				HttpResponse res = readFullResponse();
				close();
				return res;
			} else {
				// Invalid connection
				return null;
			}
			
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public RobotsTxtInfo getRobot(String rootUrl) throws IOException {
		try {
			String url = rootUrl + "robots.txt";
			urlObj = new URL(url);
			if (urlObj.getProtocol().equals("http")) {
				// Create a socket connection
				connectHttp(urlObj.getHost());
				sendGetReq();
				
				// Read in response
				HttpResponse res = readHeadResponse();
				RobotsTxtInfo robot = readRobotResponse(rootUrl, res.getDocLength());
				close();
				return robot;
			} else if (urlObj.getProtocol().equals("https")){
				// Create a URLConnection
				this.conn = (HttpsURLConnection) urlObj.openConnection();
				this.conn.setRequestMethod("GET");
				this.conn.setRequestProperty("Host", urlObj.getHost());
				this.conn.setRequestProperty("User-Agent", "cis455crawler");
				
				// Read in response
				this.in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				HttpResponse res = readHeadResponse();
				RobotsTxtInfo robot = readRobotResponse(rootUrl, res.getDocLength());
				close();
				return robot;
			} else {
				// Invalid connection
				return null;
			}
			
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	// Given a url, try to establish a connection in or out
	private boolean connectHttp(String host) throws IOException {
		InetAddress addr = InetAddress.getByName(host);
		this.s = new Socket(addr, 80);
		this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())));
		this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		return true;
	}
		
	//
	// Http Request and Response Handling
	//
	
	private void sendHeadReq(Date lastCrawled) throws IOException {
		String host = urlObj.getHost();
		String path = urlObj.getFile();
		out.println("HEAD " + path + " HTTP/1.1");
		out.println("Host: " + host);
		out.println("User-Agent: cis455crawler");
		if (lastCrawled != null) {
			SimpleDateFormat date = new SimpleDateFormat(this.dateFormat);
			out.println("If-Modified-Since: " + date.format(lastCrawled));
		}
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
				if (contentType != null) {
					int semi = contentType.indexOf(";");
					if (semi != -1) contentType = contentType.substring(0, semi);
				}
				return new HttpResponse(this.urlObj, status, contentLength, contentType);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
				
				res.setDocument(body.toString());
				return res;
			} else if (res.isXml()) {
				StringBuilder body = new StringBuilder();
				long len = res.getDocLength();
				while (len > 0) {
					body.append((char) in.read());
					len --;
				}

				res.setDocument(body.toString());
				return res;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		} 
	}
	
	private RobotsTxtInfo readRobotResponse(String rootUrl, long len) throws IOException {
		RobotsTxtInfo r = new RobotsTxtInfo(rootUrl);

		String agent = null;
		String line = "";
		while (len > 0) {
			line = in.readLine();
			if (line.indexOf("User-agent") != -1) {
				// User-agent line
				agent = line.substring(line.indexOf(':') + 1).trim();
				r.addUserAgent(agent);
			} else if (line.indexOf("Disallow") != -1) {
				// Disallow line
				String path = line.substring(line.indexOf(':') + 1).trim();
				r.addDisallowedLink(agent, path);
			} else if (line.indexOf("Allow") != -1) {
				// Allow line
				String path = line.substring(line.indexOf(':') + 1).trim();
				r.addAllowedLink(agent, path);
			} else if (line.indexOf("Crawl-delay") != -1) {
				// Delay line
				int delay = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
				r.addCrawlDelay(agent, delay);
			}
			if (!line.isEmpty()) len -= (line.length() + 1);
			else len -= 1;
		}
		
		return r;
	}
	
	public void close() throws IOException {
		if (this.out != null) this.out.close();
		if (this.in != null) this.in.close();
		if (this.s != null) this.s.close();
		if (this.conn != null) this.conn.disconnect();
	}
	
}
