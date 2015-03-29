package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
	    request.getSession(true);
	    
		try {
			Cookie[] cookies = request.getCookies();
			for (Cookie c : cookies) {
				if (c.getName().equals("cis455session")) {
					c.setMaxAge(0); // delete cookie
					response.addCookie(c);
				}
			}
			
			PrintWriter out = response.getWriter();
		    out.println("<html><head><title>XPath Servlet - Logout</title></head><body>");	
		    out.println("<h3>Logout Successful</h3>");
		    out.println("</body></html>");
			
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
