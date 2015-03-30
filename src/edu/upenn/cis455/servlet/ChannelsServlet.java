package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDB;
import edu.upenn.cis455.storage.DBWrapper;

@SuppressWarnings("serial")
public class ChannelsServlet extends HttpServlet {
	
	private DBWrapper createDB(String dir) throws Exception {
		return new DBWrapper(dir);
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		response.setContentType("text/html");
	    request.getSession(true);
	    
		try {			
		    // Set up DB
	    	DBWrapper db = createDB(getServletContext().getInitParameter("BDBstore"));
	    	ChannelDB channelDB = db.getChannelDB();
	    	
	    	ArrayList<Channel> channels = channelDB.getallChannels();
	    	db.close();
	    	
		    for (int i = 0; i < channels.size(); i++) {
		    	Channel c = channels.get(i);
		    }
	    	
			String user = null;
			Cookie[] cookies = request.getCookies();
			for (Cookie c : cookies) {
				if (c.getName().equals("cis455session")) user = c.getValue();
			}
	    	
			PrintWriter out = response.getWriter();
		    out.println("<html><head><title>XPath Servlet - Channels</title></head><body>");	
		    out.println("<h1>Channels</h1>");
		    out.println("<p>Total Channels: " + channels.size() + "</p><br />");
		    out.println("<ul>");
		    for (int i = 0; i < channels.size(); i++) {
		    	Channel c = channels.get(i);
		    	if (c.getCreator().equals(user)) {
		    		out.println("<li>" + c.getName() + "<a href=\"/HW2/delete?name=" + c.getName() + "\">  Delete</a></li>");
		    	} else {
		    		out.println("<li>" + c.getName() + "</li>");
		    	}
		    }
		    out.println("</ul>");
		    out.println("</body></html>");

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
