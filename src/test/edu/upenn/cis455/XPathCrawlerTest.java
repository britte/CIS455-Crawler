package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.PriorityQueue;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.servlet.HttpClient;


public class XPathCrawlerTest {
	
	XPathCrawler crawler;
	
	@Before
    public void setUp() {
		crawler = new XPathCrawler();
    }
 
	@Test
	public void testGetUrlsHttp() {
		HttpClient client = new HttpClient("http://www.junumusic.com/");
		Document d = client.getDoc();
		
		crawler.setCurrentClient(client);
		crawler.getUrls(d);
		assertEquals(crawler.getUrlQueue().size(), 15);
	}
	
	@Test
	public void testGetUrlsHttps() {
		HttpClient client = new HttpClient("https://dbappserv.cis.upenn.edu/crawltest.html");
		Document d = client.getDoc();
		
		crawler.setCurrentClient(client);
		crawler.getUrls(d);
		assertEquals(crawler.getUrlQueue().size(), 9);
	}
	
	@Test
	public void testCleanUrl() {		
		HttpClient client = new HttpClient("https://dbappserv.cis.upenn.edu/crawltest/");
		Document d = client.getDoc();
		
		crawler.setCurrentClient(client);
		
		assertEquals(crawler.cleanUrl("http://test.com", d), "http://test.com");
		assertEquals(crawler.cleanUrl("nytimes/", d), "https://dbappserv.cis.upenn.edu/crawltest/nytimes/");
		assertEquals(crawler.cleanUrl("/nytimes/", d), "https://dbappserv.cis.upenn.edu/nytimes/");
	}
	
	@Test
	public void testMaxDocSize() {		
		HttpClient client = new HttpClient("https://dbappserv.cis.upenn.edu/crawltest/");
		Document d = client.getDoc();
		
		crawler.setCurrentClient(client);
//		crawler.setMaxDocLength(1);
		
		assertEquals(crawler.cleanUrl("http://test.com", d), "http://test.com");
		assertEquals(crawler.cleanUrl("nytimes/", d), "https://dbappserv.cis.upenn.edu/crawltest/nytimes/");
		assertEquals(crawler.cleanUrl("/nytimes/", d), "https://dbappserv.cis.upenn.edu/nytimes/");
	}
	
}
