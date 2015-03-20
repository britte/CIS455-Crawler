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

import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class XPathEngineImplTest {
	
	XPathEngineImpl x;
	Document d; 

	@Before
    public void setUp() throws ParserConfigurationException, SAXException, IOException {
        x = new XPathEngineImpl();
        String[] paths = new String[]{
    		"/foo/bar/xyz", // basic path
    		"", // empty path
    		"foo/bar/xyz", // no initial axis
    		"/*", // invalid nodename character
    		"/a/b/c[text()=\"theEntireText\"]", // text() test
    		"/a/b/c[text() = \"white Spaces  ShouldNotMatter\"]", // text() test with whitespace 
    		"/xyz/abc[contains(text(),\"someSubstring\")]", // contains(text(), "...") test
    		"/foo/bar[@att=\"123\"]" // @attribute test
        };
        x.setXPaths(paths);
        
        // Set up document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        File f = new File("test/test.xml");
        d = builder.parse(f);
        
    }
 
	
//	@Test
//	public void isValidBasic() {
//		assertTrue(x.isValid(0));
//	}
//	
//	@Test
//	public void isValidEmpty() {
//		assertFalse(x.isValid(1));
//	}
//	
//	@Test
//	public void isValidInitialAxis() {
//		assertFalse(x.isValid(2));
//	}
//	
//	@Test
//	public void isValidNodenameChar() {
//		assertFalse(x.isValid(3));
//	}
//	
//	@Test
//	public void isValidTextTest() {
//		assertTrue(x.isValid(4));
//	}
//	
//	@Test
//	public void isValidTextTestWhitespace() {
//		assertTrue(x.isValid(5));
//	}
//	
//	@Test
//	public void isValidContainsTest() {
//		assertTrue(x.isValid(6));
//	}
//	
//	@Test
//	public void isValidAttributeTest() {
//		assertTrue(x.isValid(7));
//	}
	
	// Tests for evaluate() 
	
	@Test
	public void getTests() {
		assertEquals("Single test length", x.getTest("/foo/bar[@att=\"123\"]").size(), 1);
		assertEquals("Single test contents", x.getTest("/foo/bar[@att=\"123\"]").get(0), "@att=\"123\"");
		
		assertEquals("Multiple tests length", x.getTest("/d/e/f[foo][bar]").size(), 2);
		assertEquals("Multiple tests contents 1", x.getTest("/d/e/f[foo][bar]").get(0), "foo");
		assertEquals("Multiple tests contents 2", x.getTest("/d/e/f[foo][bar]").get(1), "bar");
		
		assertEquals("Nested tests length", x.getTest("/d/e/f[foo[zoo]][bar]").size(), 2);
		assertEquals("Nested tests contents 1", x.getTest("/d/e/f[foo[zoo]][bar]").get(0), "foo[zoo]");
		assertEquals("Nested tests contents 2", x.getTest("/d/e/f[foo[zoo]][bar]").get(1), "bar");
	}

	@Test
	public void compareTests() {
		
		Node root = d.getDocumentElement();
		
		System.out.println(root.getChildNodes().item(0));
		
		ArrayList<String> arr = new ArrayList<String>();
		
		arr.add("text()=\"Fido\"");
//		assertEquals("Text test: pet's name is Fido", x.compareTests(arr, fido.getFirstChild()), true);
	}
	
}
