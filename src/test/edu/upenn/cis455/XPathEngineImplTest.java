package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class XPathEngineImplTest {
	
	XPathEngineImpl x;

	@Before
    public void setUp() {
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

}
