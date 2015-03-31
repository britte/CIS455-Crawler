package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sleepycat.je.Transaction;

import edu.upenn.cis455.storage.CrawlDoc;
import edu.upenn.cis455.storage.CrawlDocDB;
import edu.upenn.cis455.storage.DBWrapper;

@SuppressWarnings("serial")
public class CrawledServlet extends HttpServlet {
	
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
	    	CrawlDocDB crawlDB = db.getCrawlDocDB();
	    	
	    	Transaction t = db.getTransaction();
	    	ArrayList<CrawlDoc> docs = crawlDB.getallDocs();
	    	t.commit();
	    	
			String user = null;
			Cookie[] cookies = request.getCookies();
			for (Cookie c : cookies) {
				if (c.getName().equals("cis455session")) user = c.getValue();
			}
	    	
			PrintWriter out = response.getWriter();
		    out.println("<html><head><title>XPath Servlet - Docs</title></head><body>");	
		    out.println("<h1>Docs</h1>");
		    out.println("<p>Total Docs: " + docs.size() + "</p><br />");
		    out.println("<ul>");
		    for (int i = 0; i < docs.size(); i++) {
		    	CrawlDoc d = docs.get(i);
	    		out.println("<li>" + d.getUrl() + " : " + d.getLastCrawled() + "</li>");
		    }
		    out.println("</ul>");
		    out.println("</body></html>");

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
