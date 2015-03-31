package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.PriorityQueue;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.httpclient.HttpClient;
import edu.upenn.cis455.httpclient.HttpResponse;


public class XPathCrawlerTest {
	
	XPathCrawler crawler;
	
	@Before
    public void setUp() {
		crawler = new XPathCrawler();
    }
 
	@Test
	public void testGetUrlsHttp() throws IOException {
		HttpClient client = new HttpClient();
		HttpResponse res = client.getResponse("http://www.junumusic.com/");
		Document d = res.getDoc();
		
		crawler.setCurrentResponse(res);
		crawler.getUrls(d);
		assertEquals(crawler.getUrlQueue().size(), 15);
	}
	
	@Test
	public void testGetUrlsHttps() throws IOException {
		HttpClient client = new HttpClient();
		HttpResponse res = client.getResponse("https://dbappserv.cis.upenn.edu/crawltest.html");
		Document d = res.getDoc();
		
		crawler.setCurrentResponse(res);
		crawler.getUrls(d);
		assertEquals(crawler.getUrlQueue().size(), 9);
	}
	
	@Test
	public void testCleanUrl() throws IOException {		
		HttpClient client = new HttpClient();
		HttpResponse res = client.getResponse("https://dbappserv.cis.upenn.edu/crawltest/");
		Document d = res.getDoc();
		
		crawler.setCurrentResponse(res);
		
		assertEquals(crawler.cleanUrl("http://test.com", d), "http://test.com");
		assertEquals(crawler.cleanUrl("nytimes/", d), "https://dbappserv.cis.upenn.edu/crawltest/nytimes/");
		assertEquals(crawler.cleanUrl("/nytimes/", d), "https://dbappserv.cis.upenn.edu/nytimes/");
	}
	
	@Test
	public void testMaxDocSize() throws IOException {		
		HttpClient client = new HttpClient();
		HttpResponse res = client.getResponse("https://dbappserv.cis.upenn.edu/crawltest/");
		Document d = res.getDoc();
		
		crawler.setCurrentResponse(res);
		
		assertEquals(crawler.cleanUrl("http://test.com", d), "http://test.com");
		assertEquals(crawler.cleanUrl("nytimes/", d), "https://dbappserv.cis.upenn.edu/crawltest/nytimes/");
		assertEquals(crawler.cleanUrl("/nytimes/", d), "https://dbappserv.cis.upenn.edu/nytimes/");
	}
	
}
