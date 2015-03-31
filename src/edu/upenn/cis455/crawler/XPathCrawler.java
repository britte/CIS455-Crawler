package edu.upenn.cis455.crawler;

import java.io.IOException;
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

import com.sleepycat.je.Transaction;

import edu.upenn.cis455.httpclient.HttpClient;
import edu.upenn.cis455.httpclient.HttpResponse;
import edu.upenn.cis455.httpclient.RobotsTxtInfo;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDB;
import edu.upenn.cis455.storage.CrawlDoc;
import edu.upenn.cis455.storage.CrawlDocDB;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;


public class XPathCrawler {
	
	private static HashMap<String, XPathEngineImpl> channels = new HashMap<String, XPathEngineImpl>();
	
	private static LinkedList<String> crawlUrls = new LinkedList<String>();
	private static HashSet<String> seenUrls = new HashSet<String>();
	
	private static long maxDocLength;
	private static int maxNumDocs = -1;
	private static int docsDownloaded = 0;
	
	private static HttpClient client = new HttpClient();
	private static HttpResponse res;
	
	private static DBWrapper db;
	
	public XPathCrawler() {
		channels = new HashMap<String, XPathEngineImpl>();
		crawlUrls = new LinkedList<String>();
		seenUrls = new HashSet<String>();
		client = new HttpClient();
	}
		
	public static void crawl() throws Exception {
		
		CrawlDocDB crawlDB = db.getCrawlDocDB();
		CrawlDoc doc = null;
		
		// Get a valid document off of the crawl queue
		boolean validUrl = false;
		String url = "";
		while (!validUrl) {
			url = crawlUrls.poll();
			if (url != null) {
				// Check if url matches a previously crawled document
		    	doc = crawlDB.getDocByUrl(url);
		    	if (doc != null) { // seen before
		    		res = client.getHead(url, doc.getLastCrawled());
		    	} else { // not previously seen
					res = client.getHead(url, null);	
		    	}
				validUrl = (res != null);
			} else {
				break;
			}
		}
		
		if (!validUrl || res == null) return; // the queue is empty
		
		if (res.getDocLength() <= maxDocLength) {
			// Check against robots.txt to confirm that page can be explored
			boolean allowed = robotAllowed(url);
			
			// If download allowed, get the page Document
			Document d = null;
			if (!allowed) {
				System.out.println(url + ": Access denied");
				return;
			} 
			
			// Get Document
			if (res.getStatus() == 304) {
				// If document has been explored after last modification, get stored file
				d = doc.getDoc();

				// Report download success/failure
				if (d == null) {
					System.out.println(url + ": DOWNLOAD FAILED"); 
					return;
				} else {
		    		docsDownloaded += 1;
					System.out.println(url + ": Not modified" + (maxNumDocs == -1 ? "" : (" (" + docsDownloaded + "/" + maxNumDocs + ")")));
				}
			} else {
				// If document hasn't been explored since last modification download it
				res = client.getResponse(url);
				if (res == null) {
					System.out.println(url + ": DOWNLOAD FAILED"); 
					return;
				}
						
				d = res.getDoc();
				
				// Report download success/failure
				if (d == null) {
					System.out.println(url + ": DOWNLOAD FAILED"); 
					return;
				} else {
					// Mark url (re)crawled
					Transaction t = db.getTransaction();
			    	if (doc == null) { // never previously seen
			    		doc = new CrawlDoc(url, res.getDocString(), res.getDocType());
			    		crawlDB.insertCrawlDoc(doc, t);
			    		docsDownloaded += 1;
						System.out.println(url + ": Downloaded" + (maxNumDocs == -1 ? "" : (" (" + docsDownloaded + "/" + maxNumDocs + ")")));
			    	} else { // previously seen
			    		crawlDB.updateCrawlDoc(doc, t);
			    		docsDownloaded += 1;
						System.out.println(url + ": Updated" + (maxNumDocs == -1 ? "" : (" (" + docsDownloaded + "/" + maxNumDocs + ")")));
			    	}
			    	t.commit();
				}
			}
			
			
			// Handle further document processing by type
			if (res.isXml()) {
				// If the current document is xml, check against tracked channels
				compareChannels(doc);
			} else if (res.isHtml()) {
				// If the current document is html, explore it for unseen links
				getUrls(d);
			}	
			res = null;
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
	        	}
	        }
	    }
	}
	
	// Given an href url, return an expanded url based on whether
	// it is absolute or relative
	public static String cleanUrl(String url, Document d) {
		if (res == null || url == null) return null;
		String baseUrl = res.getBaseUrl();
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
				return res.getRootUrl() + url.substring(1);	
			} else { 
				// ensure that a subpath that is NOT a file has a trailing "/"
				if (!url.endsWith("/")) url = url + "/";
				return res.getRootUrl() + url.substring(1);
			}
		} else {
			// directory relative path (format "path/subpath")
			if (url.indexOf('.') != -1) { // file path
				return res.getBaseUrl() + url;	
			} else { 
				// ensure that a subpath that is NOT a file has a trailing "/"
				if (!url.endsWith("/")) url = url + "/";
				return res.getBaseUrl() + url;
			}
		}
	}
	
	// Determine if a given allowed by the robots.txt file (if one exits)
	private static boolean robotAllowed(String url) throws IOException {
		RobotsTxtInfo robot = client.getRobot(res.getRootUrl());
		if (robot != null) {
			if (robot.containsUserAgent("cis455crawler")) {
				return robot.checkLink("cis455crawler", url);
			} else if (robot.containsUserAgent("*")) {
				return robot.checkLink("*", url);
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
	
	private static void compareChannels(CrawlDoc doc) throws Exception {
		Iterator it = channels.entrySet().iterator();
    	ChannelDB channelDB = db.getChannelDB();
	    while (it.hasNext()) {
	        Map.Entry channelEngine = (Map.Entry) it.next();
	        String channelName = (String) channelEngine.getKey();
	        XPathEngineImpl xpathEngine = (XPathEngineImpl) channelEngine.getValue();
	        boolean[] match = xpathEngine.evaluate(doc.getDoc());
	        for (int i=0; i < match.length; i++) {
	        	if (match[i]) {
	        		Transaction t = db.getTransaction();
	        		Channel c = channelDB.getChannelByName(channelName);
	        		c.addMatch(doc.getUrl());
	        		channelDB.updateChannel(c);
	        		t.commit();
	        		break;
	        	}
	        }
	    }
	}
	
	private static void setUpChannels() throws Exception {
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
				db = new DBWrapper(dbPath);
				setUpChannels();
				
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
				e.printStackTrace();
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
	
	public void setCurrentResponse(HttpResponse res) {
		this.res = res;
	}
	
	public void setMaxDocLength(long len) {
		this.maxDocLength = len;
	}
	
	public void setMaxNumDocs(int num) {
		this.maxNumDocs = num;
	}
}
