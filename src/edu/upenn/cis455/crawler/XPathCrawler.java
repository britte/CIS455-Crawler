package edu.upenn.cis455.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.upenn.cis455.servlet.HttpClient;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDB;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;


public class XPathCrawler {
	
	private static HashMap<String, XPathEngineImpl> channels = new HashMap<String, XPathEngineImpl>();
	
	private static LinkedList<String> crawlUrls = new LinkedList<String>();
	private static HashSet<String> seenUrls = new HashSet<String>();
	
	private static long maxDocLength;
	private static int maxNumDocs = -1;
	private static int docsDownloaded = 0;
	
	private static HttpClient currentClient;
	
	public XPathCrawler() {
		channels = new HashMap<String, XPathEngineImpl>();
		crawlUrls = new LinkedList<String>();
		seenUrls = new HashSet<String>();
	}
		
	public static void crawl() {
		// Get a valid document off of the crawl queue
		boolean validUrl = false;
		String url = "";
		
		while (!validUrl) {
			url = crawlUrls.poll();
			if (url != null) {
				currentClient = new HttpClient(url);
				validUrl = currentClient.isValid();
			} else {
				break;
			}
		}
		
		if (!validUrl || currentClient == null) { // the queue is empty
			return;
		} else if (currentClient.getDocLength() <= maxDocLength) {
			// TODO: check if url has been modified since the last crawl
			docsDownloaded += 1;
			System.out.println(url + ": Downloading" + 
								(maxNumDocs == -1 ? "" : (" (" + docsDownloaded + "/" + maxNumDocs + ")")));
			Document d = currentClient.getDoc();
			if (currentClient.isXml()) {
				// If the current document is xml, check against tracked channels
				compareChannels(d);
			} else if (currentClient.isHtml()) {
				// If the current document is html, explore it for unseen links
				getUrls(d);
			}	
			currentClient = null;
		}
	}
	
	public static void getUrls(Document d) {
		if (d == null) return;
		// Find all link elements (<a>)
		NodeList links = d.getElementsByTagName("a");
		
		for (int i = 0; i < links.getLength(); i++) {
	        Node link = links.item(i);
	        if (link.getNodeType() == Node.ELEMENT_NODE) {
	            // Get href and clean up
	        	Element e = (Element) link;
	        	String href = cleanUrl(e.getAttribute("href"), d);
	        	// If url is new, add it to the seen list and the crawl queue
	        	if (!href.isEmpty() && !seenUrls.contains(href)) {
	        		seenUrls.add(href);
	        		crawlUrls.add(href);
//	        		System.out.println("Resource discovered: " + href);
	        	}
	        }
	    }
	}
	
	// Given an href url, return an expanded url based on whether
	// it is absolute or relative
	public static String cleanUrl(String url, Document d) {
		if (currentClient == null || url == null) return null;
		String baseUrl = currentClient.getBaseUrl(d);
		if (url.indexOf("http://") != -1 || url.indexOf("https://") != -1) {
			// absolute href 
			return url;
		} else if (url.indexOf(':') != -1) {
			// invalid scheme type
			// TODO: handle this more carefully
			return null;
		} else if (url.indexOf('/') == 0) {
			// root relative path (format: "/path/subpath")
			if (url.indexOf('.') != -1) { // file path
				return currentClient.getRootUrl() + url.substring(1);	
			} else { 
				// ensure that a subpath that is NOT a file has a trailing "/"
				if (!url.endsWith("/")) url = url + "/";
				return currentClient.getRootUrl() + url.substring(1);
			}
		} else {
			// directory relative path (format "path/subpath")
			if (url.indexOf('.') != -1) { // file path
				return currentClient.getBaseUrl(d) + url;	
			} else { 
				// ensure that a subpath that is NOT a file has a trailing "/"
				if (!url.endsWith("/")) url = url + "/";
				return currentClient.getBaseUrl(d) + url;
			}
		}
	}
	
	private static void compareChannels(Document d) {
		Iterator it = channels.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry channelEngine = (Map.Entry) it.next();
	        XPathEngineImpl xpathEngine = (XPathEngineImpl) channelEngine.getValue();
	        boolean[] match = xpathEngine.evaluate(d);
	        for (int i=0; i < match.length; i++) {
	        	if (match[i]) {
	        		// Add channel --> document mapping
	        		break;
	        	}
	        }
	        it.remove(); 
	    }
	}
	
	private static void setUpChannels(DBWrapper db) {
    	ChannelDB channelDB = db.getChannelDB();
    	ArrayList<Channel> cs = channelDB.getallChannels();
		System.out.println("Total channels: " + cs.size());
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
				seenUrls.add(startURL);
				crawlUrls.add(startURL);
				
				// 2. The directory containing the BerkeleyDB environment that holds your store.
				// 	  The directory should be created if it does not already exist.
				String dbPath = args[1];
				DBWrapper db = new DBWrapper(dbPath);
				setUpChannels(db);
				
				// 3. Max size, in megabytes, of a document to be retrieved from a Web server
				int mb = Integer.parseInt(args[2]);
				maxDocLength = mb * 1000000; // conver megabytes to bytes
				
				// 4. (Optional) Number of files to search before stopping
				if (args.length == 4) maxNumDocs = Integer.parseInt(args[3]);
				
				// Start the crawler
				while (!crawlUrls.isEmpty() && (maxNumDocs == -1 || docsDownloaded < maxNumDocs)) {
					crawl();
				}
				
			} catch (Exception e) {
				System.out.println(e);
				throw new IllegalArgumentException("Usage: <start-url> <db-directory> <max-file-size> [<max-num-files>]");
			}
		}
	}
	
	//
	// Methods for testing
	//
	public LinkedList<String> getUrlQueue() {
		return this.crawlUrls;
	}
	
	public void setCurrentClient(HttpClient c) {
		this.currentClient = c;
	}
	
	public void setMaxDocLength(long len) {
		this.maxDocLength = len;
	}
	
	public void setMaxNumDocs(int num) {
		this.maxNumDocs = num;
	}
}
