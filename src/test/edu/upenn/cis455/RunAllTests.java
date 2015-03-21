package test.edu.upenn.cis455;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    // Class.forName("your.class.name.here") 
	test.edu.upenn.cis455.XPathEngineImplTest.class,
	test.edu.upenn.cis455.XPathServletTest.class
})

public class RunAllTests {
}
