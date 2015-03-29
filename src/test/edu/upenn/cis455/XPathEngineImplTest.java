package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.upenn.cis455.servlet.HttpClient;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class XPathEngineImplTest {
	
	XPathEngineImpl x;
	XPathEngineImpl x2;
	Document d; 
	Document webd;

	@Before
    public void setUp() throws ParserConfigurationException, SAXException, IOException {
		// Set up for isValid
        x = new XPathEngineImpl();
        String[] paths = new String[]{
    		"/foo/bar/xyz", // basic path
    		"", // empty path
    		"foo/bar/xyz", // no initial axis
    		"/*", // invalid nodename character
    		"/a/b/c[text()=\"theEntireText\"]", // text() test
    		"/a/b/c[text() = \"white Spaces  ShouldNotMatter\"]", // text() test with whitespace 
    		"/xyz/abc[contains(text(),\"someSubstring\")]", // contains(text(), "...") test
    		"/foo/bar[@att=\"123\"]", // @attribute test
    		"/foo[bar]", // first step test
    		"/foo[bar[bim]]", // nested tests
    		"/foo[bar][bim]" // mulitple tests
        };
        x.setXPaths(paths);
        
        // Set up for evaluate
        x2 = new XPathEngineImpl();
        String[] paths2 = new String[]{
    		"/pets/pet", // basic steps
    		"/pets[pet]", // test step
    		"/pets/pet[@type=\"dog\"]", // test attribute
    		"/pets[pet[@type=\"dog\"]]", // test nested
    		"/pets[name]", // proper depth
    		"/pets/pet[@type=\"dog\"][@type=\"cat\"]", // multiple tests
    		"/pets[pet][owner]"
        };
        x2.setXPaths(paths2);
        // Set up document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        File f = new File("test/test.xml");
        d = builder.parse(f);
   
    }
 
	
	@Test
	public void isValidBasic() {
		assertTrue(x.isValid(0));
	}
	
	@Test
	public void isValidEmpty() {
		assertFalse(x.isValid(1));
	}
	
	@Test
	public void isValidInitialAxis() {
		assertFalse(x.isValid(2));
	}
	
	@Test
	public void isValidNodenameChar() {
		assertFalse(x.isValid(3));
	}
	
	@Test
	public void isValidTextTest() {
		assertTrue(x.isValid(4));
	}
	
	@Test
	public void isValidTextTestWhitespace() {
		assertTrue(x.isValid(5));
	}
	
	@Test
	public void isValidContainsTest() {
		assertTrue(x.isValid(6));
	}
	
	@Test
	public void isValidAttributeTest() {
		assertTrue(x.isValid(7));
	}
	
	@Test
	public void isValidFirstStepTest() {
		assertTrue(x.isValid(8));
	}
	
	@Test
	public void isValidNestedTest() {
		assertTrue(x.isValid(9));
	}
	
	@Test
	public void isValidMultipleTests() {
		assertTrue(x.isValid(10));
	}
	
	// Tests for evaluate() 
	
	@Test
	public void getTests() {
		assertEquals("Single test length", x.getTests("/foo/bar[@att=\"123\"]").size(), 1);
		assertEquals("Single test contents", x.getTests("/foo/bar[@att=\"123\"]").get(0), "@att=\"123\"");
		
		assertEquals("Multiple tests length", x.getTests("/d/e/f[foo][bar]").size(), 2);
		assertEquals("Multiple tests contents 1", x.getTests("/d/e/f[foo][bar]").get(0), "foo");
		assertEquals("Multiple tests contents 2", x.getTests("/d/e/f[foo][bar]").get(1), "bar");
		
		assertEquals("Nested tests length", x.getTests("/d/e/f[foo[zoo]][bar]").size(), 2);
		assertEquals("Nested tests contents 1", x.getTests("/d/e/f[foo[zoo]][bar]").get(0), "foo[zoo]");
		assertEquals("Nested tests contents 2", x.getTests("/d/e/f[foo[zoo]][bar]").get(1), "bar");
	}

	@Test
	public void compareTests() {
		
		Node root = d.getDocumentElement();
		Node fido = root.getChildNodes().item(1);
		
		assertTrue("Text test: pet's name is Fido", x.compareTest("text()=\"Fido\"", fido.getChildNodes().item(1)));
		assertTrue("Contains test: pet's name contains 'Fi'", x.compareTest("contains(text(), \"Fi\")", fido.getChildNodes().item(1)));
		assertFalse("Contains test: pet's name doesn't contain 'x'", x.compareTest("contains(text(), \"x\")", fido.getChildNodes().item(1)));
		assertTrue("Att test: pet type is dog", x.compareTest("@type=\"dog\"", fido));

	}
	
	@Test 
	public void evaluateTestLocal(){
		boolean[] results = x2.evaluate(d);
		
		assertTrue(results[0]);
		assertTrue(results[1]);
		assertTrue(results[2]);
		assertTrue(results[3]);
		assertFalse(results[4]);
		assertTrue(results[5]);
		assertTrue(results[6]);
	}
	
	@Test
	public void evaluateTestWeb() throws IOException{
        // Set up online document
        HttpClient client = new HttpClient("http://www.w3schools.com/xml/note.xml");
        webd = client.getDoc();
        client.close();
        
		XPathEngineImpl x3 = new XPathEngineImpl();
        String[] paths = new String[]{
    		"/note/to" // basic steps
        };
        
        x3.setXPaths(paths);
		boolean[] results = x3.evaluate(webd);
		assertTrue(results[0]);
	}
}
