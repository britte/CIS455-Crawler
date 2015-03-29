package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.ChannelDB;
import edu.upenn.cis455.storage.DBWrapper;

@SuppressWarnings("serial")
public class DeleteChannelServlet extends HttpServlet {
	
	private DBWrapper createDB(String dir) throws Exception {
		return new DBWrapper(dir);
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
	    request.getSession(true);
	    
		try {
		    // Set up DB
	    	DBWrapper db = createDB(getServletContext().getInitParameter("BDBstore"));
	    	ChannelDB channelDB = db.getChannelDB();
	    	
		    String name = request.getParameter("name");
	    	
			// Check that a user is currently logged in
			String user = null;
			Cookie[] cookies = request.getCookies();
			for (Cookie c : cookies) {
				if (c.getName().equals("cis455session")) user = c.getValue();
			}
	    	System.out.println(name);
	    	boolean removeSuccess = channelDB.removeChannel(name, user);
	    	db.close();
	    	
	    	if (removeSuccess) {
				PrintWriter out = response.getWriter();
			    out.println("<html><head><title>XPath Servlet - Delete Channel</title></head><body>");	
			    out.println("<h3>Channel Successfully Deleted</h3>");
			    out.println("</body></html>");
	    	} else {
				PrintWriter out = response.getWriter();
			    out.println("<html><head><title>XPath Servlet - Delete Channel</title></head><body>");	
			    out.println("<h3>Channel could not be successfully deleted</h3>");
			    out.println("</body></html>");	    		
	    	}
			
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
