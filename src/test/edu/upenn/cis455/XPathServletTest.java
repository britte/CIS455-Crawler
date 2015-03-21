package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.upenn.cis455.servlet.XPathServlet;

public class XPathServletTest {
	
	PrintWriter out;
	BufferedReader in;

	@Before
    public void setUp() throws ParserConfigurationException, SAXException, IOException {
		InetAddress addr = InetAddress.getLocalHost();
		Socket s = new Socket(addr, 8080);
		this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())));
		this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }
 
	@Test
	public void testGet() throws IOException{
		out.println("GET /HW2/xpath HTTP/1.1");
		out.println("Host: localhost:8080");
		out.println();
		out.flush();
		
		assertEquals(in.readLine(), "HTTP/1.1 200 OK");
	}
	
	@Test
	public void testPost() throws IOException{
		
		String params = "xpath=hello&url=www.world.com/hello";
		
		out.println("POST localhost:8080/HW2/xpath HTTP/1.1");
		out.println("Content-Type: application/x-www-form-urlencoded");
		out.println("Content-Length: " + params.length());
		out.println();
		out.println(params);
		out.println();
		out.flush();
		
//		assertEquals(in.readLine(), "HTTP/1.1 200 OK");
	}

}
