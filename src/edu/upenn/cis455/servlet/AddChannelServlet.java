package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sleepycat.je.Transaction;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDB;
import edu.upenn.cis455.storage.DBWrapper;

@SuppressWarnings("serial")
public class AddChannelServlet extends HttpServlet {
	
	private DBWrapper createDB(String dir) throws Exception {
		return new DBWrapper(dir);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		response.setContentType("text/html");
	    request.getSession(true);
	    
		try {			
		    // Set up DB
	    	DBWrapper db = createDB(getServletContext().getInitParameter("BDBstore"));
	    	ChannelDB channelDB = db.getChannelDB();
	    		    	
	    	// Get channel params
		    String name = request.getParameter("name");
		    String xsl = request.getParameter("xsl");
		    String[] xpaths = request.getParameter("xpaths").split("|");
		    for (int i = 0; i < xpaths.length; i++) {
		    	xpaths[i] = xpaths[i].trim();
		    }
		    
			// Check that a user is currently logged in
			String user = null;
			Cookie[] cookies = request.getCookies();
			for (Cookie c : cookies) {
				if (c.getName().equals("cis455session")) user = c.getValue();
			}
		    
			Transaction t = db.getTransaction();
			boolean insertSuccess = channelDB.insertChannel(name, xpaths, xsl, user);
			t.commit();
			
		    if (name.isEmpty() || xsl.isEmpty() || xpaths.length == 0 || user == null || !insertSuccess) {
				PrintWriter out = response.getWriter();
			    out.println("<html><head><title>XPath Servlet - Add Channel</title></head><body>");	
			    out.println("<h3>Channel could not be successfully added!</h3>");
			    out.println("<a href=\"/HW2/addchannel\">Add channel</a>");
			    out.println("</body></html>");
		    } else {
				PrintWriter out = response.getWriter();
			    out.println("<html><head><title>XPath Servlet - Add Channel</title></head><body>");	
			    out.println("<h3>Channel " + name + " Successfully Added!</h3>");
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
		    out.println("<html><head><title>XPath Servlet - Add Channel</title></head><body>");	
		    out.println("<h1>Add Channel</h1>");
		    out.println("<form method=\"post\">");
		    out.println("Name: <input type=\"text\" name=\"name\"><br/>");
		    out.println("XPaths: (please separate with '|' symbol)<br/>");
		    out.println("<textarea rows=\"4\" cols=\"50\" name=\"xpaths\"></textarea><br/>");
		    out.println("XSL Url: <input type=\"text\" name=\"xsl\"><br/>");
		    out.println("<input type=\"submit\" value=\"Add\"></form>");
		    out.println("<a href=\"/HW2/channels\">See all channels</a>");
		    out.println("</body></html>");
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
