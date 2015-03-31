package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sleepycat.je.Transaction;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDB;
import edu.upenn.cis455.storage.CrawlDoc;
import edu.upenn.cis455.storage.CrawlDocDB;
import edu.upenn.cis455.storage.DBWrapper;

@SuppressWarnings("serial")
public class ChannelServlet extends HttpServlet {
	
	private DBWrapper createDB(String dir) throws Exception {
		return new DBWrapper(dir);
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		response.setContentType("text/xml");
	    request.getSession(true);
	    
		try {	
		    // Set up DB
	    	DBWrapper db = createDB(getServletContext().getInitParameter("BDBstore"));
	    	ChannelDB channelDB = db.getChannelDB();
	    	
	    	// Get channel
	    	Transaction t = db.getTransaction();
	    	String channelName = request.getParameter("name");
	    	Channel c = channelDB.getChannelByName(channelName);
	    	t.commit();
	    	
	    	HashSet<String> matches = (c == null) ? new HashSet<String>() : c.getMatched();
	    	Iterator<String> matchIter = matches.iterator();
	    	
	    	// Get all associated documents
	    	CrawlDocDB crawlDocDB = db.getCrawlDocDB();
	    	HashSet<CrawlDoc> docs = new HashSet<CrawlDoc>();
	    	while (matchIter.hasNext()) {
		    	String url = matchIter.next();
		    	t = db.getTransaction();
		    	CrawlDoc d = crawlDocDB.getDocByUrl(url);
		    	docs.add(d);
		    	t.commit();
		    }
	    	Iterator<CrawlDoc> docIter = docs.iterator();
	    	
			PrintWriter out = response.getWriter();
			out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
			if (!c.getXsl().isEmpty() && c.getXsl().endsWith(".xsl")) {
				out.println("<?xml-stylesheet type=\"text/xsl\" href=\"" + c.getXsl() + "\"?>");
			}
		    out.println("<documentcollection>");
		    while (docIter.hasNext()) {
		    	CrawlDoc d = docIter.next();
		    	SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		    	SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
		    	String dt = date.format(d.getLastCrawled()) + "T" + time.format(d.getLastCrawled());
	    		out.println("<document crawled=\"" + dt + "\" location=\"" + d.getUrl() + "\">");
	    		out.println(d.getDocBody());
	    		out.println("</document>");
		    }
		    out.println("</documentcollection>");

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
