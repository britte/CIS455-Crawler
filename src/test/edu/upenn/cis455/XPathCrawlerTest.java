package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.util.ArrayList;

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
		
		crawler.getUrls(d);
		assertEquals(crawler.getUrlQueue().size(), 15);
	}
	
	@Test
	public void testGetUrlsHttps() {
		HttpClient client = new HttpClient("https://dbappserv.cis.upenn.edu/crawltest.html");
		Document d = client.getDoc();
		
		crawler.getUrls(d);
		assertEquals(crawler.getUrlQueue().size(), 9);
	}
	
}
