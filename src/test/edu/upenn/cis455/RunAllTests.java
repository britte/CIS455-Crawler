package test.edu.upenn.cis455;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    // Class.forName("your.class.name.here") 
	test.edu.upenn.cis455.XPathEngineImplTest.class,
	test.edu.upenn.cis455.HttpClientTest.class,
//	test.edu.upenn.cis455.XPathServletTest.class, 
	test.edu.upenn.cis455.XPathCrawlerTest.class
})

public class RunAllTests {
}
