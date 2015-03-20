package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.*;

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
			HttpClient client = new HttpClient(url);
			PrintWriter out = response.getWriter();
		    out.println("<html><head><title>XPath Servlet</title></head><body>");		    
		    out.println("Xpath: " + xpath + "<br/>URL: " + url + "<br/>" + client.getDoc());
		    out.println("<a href=\"/HW2/xpath\">Return</a>");
		    out.println("</body></html>");
		} catch (IOException e) {
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









