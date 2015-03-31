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
			// Send head request
			if (!connect(url, false)) return null;
			sendHeadReq(lastCrawled);
			close();

			// Read in response
			if (!connect(url, true)) return null;
			HttpResponse res = readHeadResponse();
			close();
			
			return res;
		} catch (Exception e){
			e.printStackTrace();
			close();
			return null;
		}
	}
	
	public HttpResponse getResponse(String url) throws IOException {
		try {
			// Send get request
			if (!connect(url, false)) return null;
			sendGetReq();
			close();
			
			// Read in response
			if (!connect(url, true)) return null;
			HttpResponse res = readFullResponse();
			close();
			
			return res;
		} catch (Exception e){
			e.printStackTrace();
			close();
			return null;
		}
	}
	
	public RobotsTxtInfo getRobot(String rootUrl) throws IOException {
		try {
			// Send get request
			if (!connect(rootUrl + "robots.txt", false)) return null;
			sendGetReq();
			close();
			
			// Read in headers & robot file
			if (!connect(rootUrl + "robots.txt", true)) return null;
			HttpResponse res = readHeadResponse();
			RobotsTxtInfo robot = readRobotResponse(rootUrl, res.getDocLength());
			close();
			return robot;
		} catch (Exception e) {
			e.printStackTrace();
			close();
			return null;
		}
	}
	
	// Given a url, try to establish a connection in or out
	private boolean connect(String url, boolean in) {
		try {
			urlObj = new URL(url);
			if (urlObj.getProtocol().equals("http")) {
				// HTTP connection 
				InetAddress addr = InetAddress.getByName(urlObj.getHost());
				this.s = new Socket(addr, 80);
				if (in) {
					this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));	
				} else {
					this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())));
				}
				return true;
			} else if (urlObj.getProtocol().equals("https")){
				// HTTPS connection
				this.conn = (HttpsURLConnection) urlObj.openConnection();
				if (in) {
					this.conn.setDoInput(true);
					this.in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				} else {
					this.conn.setDoOutput(true);
					this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(conn.getOutputStream())));
				}
				return true;
			} else {
				// Invalid connection
				return false;
			}
			
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
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
