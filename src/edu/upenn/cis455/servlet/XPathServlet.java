package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.*;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.upenn.cis455.xpathengine.XPathEngine;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		response.setContentType("text/html");
	    request.getSession(true);
	    
	    String xpath = request.getParameter("xpath");
	    String url = request.getParameter("url");
	    
	    if (xpath == null || url == null) {
	    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    	return;
	    }
	    
		try {
			PrintWriter out = response.getWriter();
			
			// Try to get document
			HttpClient client = new HttpClient(url);
			Document d = client.getDoc();
			client.close();
			
			if (d == null) { // error connecting to server
			    out.println("<html><head><title>XPath Servlet</title></head><body>");		    
			    out.println("Error occured connecting to given url server.<br/>");
			    out.println("<a href=\"/HW2/xpath\">Home</a>");
			    out.println("</body></html>");
			} else {
				
				XPathEngineFactory factory = new XPathEngineFactory();
				XPathEngine engine = factory.getXPathEngine();
				engine.setXPaths(new String[]{xpath});
				boolean[] results = engine.evaluate(d);
				
			    out.println("<html><head><title>XPath Servlet</title></head><body>");
			    out.println("<div style=\"color:" + (results[0] ? "green" : "red") + "\">");
			    out.println("\"" + xpath + "\"" + (results[0] ? " MATCHES " : " DOES NOT MATCH ") + "\"" + url + "\"" + "</div>");
			    out.println("<a href=\"/HW2/xpath\">Home</a>");
			    out.println("</body></html>");
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		response.setContentType("text/html");
	    request.getSession(true);
		try {
			PrintWriter out = response.getWriter();
		    out.println("<html><head><title>XPath Servlet</title></head><body>");		    
		    out.println("<form method=\"post\">");
		    out.println("XPath: <input type=\"text\" name=\"xpath\"><br/>");
		    out.println("Document URL: <input type=\"text\" name=\"url\"><br/>");
		    out.println("<input type=\"submit\" value=\"Submit\"></form>");
		    out.println("</body></html>");
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}









