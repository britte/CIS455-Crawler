package edu.upenn.cis455.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

import org.w3c.dom.Document;

import edu.upenn.cis455.servlet.HttpClient;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDB;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;


public class XPathCrawler {
	
	private static HashMap<String, XPathEngineImpl> channels = new HashMap<String, XPathEngineImpl>();
	
	private PriorityQueue<String> crawlUrls = new PriorityQueue<String>();
	private HashSet<String> seenUrls = new HashSet<String>();
	
	private void crawl() {
		// Explore given document
		// Find urls
		// Parse out urls of interest
		
		// Compare document with channels
	}
	
	private void compareChannels(Document d) {
		Iterator it = channels.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry channelEngine = (Map.Entry) it.next();
	        XPathEngineImpl xpathEngine = (XPathEngineImpl) channelEngine.getValue();
	        boolean[] match = xpathEngine.evaluate(d);
	        it.remove(); 
	    }
	}
	
	private static void setUpChannels(DBWrapper db) {
    	ChannelDB channelDB = db.getChannelDB();
    	ArrayList<Channel> cs = channelDB.getallChannels();
		// Create map from each channel's name --> XPathEngine for its xpaths
		for (int i=0; i< cs.size(); i++) {
			Channel c = cs.get(i);
			XPathEngineImpl xpathEngine = new XPathEngineImpl();
			xpathEngine.setXPaths(c.getXPaths());
			channels.put(c.getName(), xpathEngine);
		}
	}
	
	public static void main(String args[]){
		// Invalid number of parameters passed in
		if (args.length < 3 || args.length > 4) {
			throw new IllegalArgumentException("Usage: <start-url> <db-directory> <max-file-size> [<max-num-files>]");
		} else {
			try {
				// TODO: validate params in some way
				// 1. The URL of the Web page at which to start.
				String startURL = args[0];
				HttpClient client = new HttpClient(startURL);
				if (!client.isValid()) throw new Exception(); 
				
				// 2. The directory containing the BerkeleyDB environment that holds your store.
				// 	  The directory should be created if it does not already exist.
				String dbPath = args[1];
				DBWrapper db = new DBWrapper(dbPath);
				setUpChannels(db);
				
				// 3. Max size, in megabytes, of a document to be retrieved from a Web server
				int maxDocSize = Integer.parseInt(args[2]);
				
				// 4. (Optional) Number of files to search before stopping
				Integer maxNumDocs; 
				if (args.length == 4) maxNumDocs = Integer.parseInt(args[3]);
			} catch (Exception e) {
				throw new IllegalArgumentException("Usage: <start-url> <db-directory> <max-file-size> [<max-num-files>]");
			}
		}
	}
}
