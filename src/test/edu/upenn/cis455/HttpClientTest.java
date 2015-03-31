package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.w3c.dom.Document;

import edu.upenn.cis455.httpclient.HttpClient;
import edu.upenn.cis455.httpclient.HttpResponse;
import edu.upenn.cis455.httpclient.RobotsTxtInfo;

public class HttpClientTest {

	@Test
	public void testHttp() throws IOException {
		HttpClient client = new HttpClient();
		HttpResponse res = client.getResponse("http://www.junumusic.com/");
		
		assertNotNull(res);
		assertTrue(res.isHtml());
		
		Document d = res.getDoc();
		assertNotNull(d);
	}
	
	@Test
	public void testHttps() throws IOException {
		HttpClient client = new HttpClient();
		HttpResponse res = client.getResponse("https://dbappserv.cis.upenn.edu/crawltest.html");
		
		assertNotNull(res);
		assertTrue(res.isHtml());
		
		Document d = res.getDoc();
		assertNotNull(d);
	}
	
	@Test
	public void testGetRelativeUrlsHasBaseElem() throws IOException {
		HttpClient client = new HttpClient();
		HttpResponse res = client.getResponse("http://www.w3schools.com/tags/tryhtml_base_test.htm");
		
		assertNotNull(res);
		assertTrue(res.isHtml());
		
		Document d = res.getDoc();
		assertNotNull(d);

		assertEquals(res.getRootUrl(), "http://www.w3schools.com/");
		assertEquals(res.getBaseUrl(), "http://www.w3schools.com/images/");
	}
	
	@Test
	public void testGetBaseUrlNoBaseElem1() throws IOException {
		HttpClient client = new HttpClient();
		HttpResponse res = client.getResponse("https://dbappserv.cis.upenn.edu/crawltest.html");
		
		assertNotNull(res);
		assertTrue(res.isHtml());
		
		Document d = res.getDoc();
		assertNotNull(d);

		assertEquals(res.getRootUrl(), "https://dbappserv.cis.upenn.edu/");
		assertEquals(res.getBaseUrl(), "https://dbappserv.cis.upenn.edu/");
	}
	
	
	@Test
	public void testGetBaseUrlNoBaseElem2() throws IOException {
		HttpClient client = new HttpClient();
		HttpResponse res = client.getResponse("https://dbappserv.cis.upenn.edu/crawltest/marie/tpc/");
		
		assertNotNull(res);
		assertTrue(res.isHtml());
		
		Document d = res.getDoc();
		assertNotNull(d);

		assertEquals(res.getRootUrl(), "https://dbappserv.cis.upenn.edu/");
		assertEquals(res.getBaseUrl(), "https://dbappserv.cis.upenn.edu/crawltest/marie/tpc/");
	}
	
	@Test
	public void testRobotsTxt() throws IOException {
		HttpClient client = new HttpClient();
		RobotsTxtInfo r = client.getRobot("https://dbappserv.cis.upenn.edu/");
		assertTrue(r.containsUserAgent("*"));
		assertTrue(r.containsUserAgent("cis455crawler"));
		ArrayList<String> disallowed = r.getDisallowedLinks("cis455crawler");
		assertTrue(disallowed.contains("/crawltest/marie/private/"));
		assertEquals(r.getRootUrl(), "https://dbappserv.cis.upenn.edu/");
	}
	
}
