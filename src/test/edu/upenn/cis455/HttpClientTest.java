package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import org.junit.Test;
import org.w3c.dom.Document;

import edu.upenn.cis455.servlet.HttpClient;

public class HttpClientTest {

	@Test
	public void testHttp() {
		HttpClient client = new HttpClient("http://www.junumusic.com/");
		
		assertTrue(client.isValid());
		assertTrue(client.isHtml());
		
		Document d = client.getDoc();
		assertNotNull(d);
	}
	
	@Test
	public void testHttps() {
		HttpClient client = new HttpClient("https://dbappserv.cis.upenn.edu/crawltest.html");
		assertTrue(client.isValid());
		assertTrue(client.isHtml());
		
		Document d = client.getDoc();
		assertNotNull(d);
	}
	
	@Test
	public void testGetRelativeUrlsHasBaseElem() {
		HttpClient client = new HttpClient("http://www.w3schools.com/tags/tryhtml_base_test.htm");
		assertTrue(client.isValid());
		assertTrue(client.isHtml());
		
		Document d = client.getDoc();
		assertNotNull(d);

		assertEquals(client.getRootUrl(), "http://www.w3schools.com/");
		assertEquals(client.getBaseUrl(d), "http://www.w3schools.com/images/");
	}
	
	@Test
	public void testGetBaseUrlNoBaseElem1() {
		HttpClient client = new HttpClient("https://dbappserv.cis.upenn.edu/crawltest.html");
		assertTrue(client.isValid());
		assertTrue(client.isHtml());
		
		Document d = client.getDoc();
		assertNotNull(d);

		assertEquals(client.getRootUrl(), "https://dbappserv.cis.upenn.edu/");
		assertEquals(client.getBaseUrl(d), "https://dbappserv.cis.upenn.edu/");
	}
	
	
	@Test
	public void testGetBaseUrlNoBaseElem2() {
		HttpClient client = new HttpClient("https://dbappserv.cis.upenn.edu/crawltest/marie/tpc/");
		assertTrue(client.isValid());
		assertTrue(client.isHtml());
		
		Document d = client.getDoc();
		assertNotNull(d);

		assertEquals(client.getRootUrl(), "https://dbappserv.cis.upenn.edu/");
		assertEquals(client.getBaseUrl(d), "https://dbappserv.cis.upenn.edu/crawltest/marie/tpc/");
	}
	
}
