package edu.upenn.cis455.xpathengine;

import org.w3c.dom.*;

import java.util.ArrayList;


public class XPathEngineImpl implements XPathEngine {
	
	String[] xpaths = new String[0];
	String path;
	int next;
	Character c;

	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor!!
	}
	
	// Set internal collection of xpaths
	public void setXPaths(String[] s) {
		this.xpaths = s;
	}

	// Given an index, determine if the associated xpath 
	// has a valid format based on our grammar
	public boolean isValid(int i) {
		this.path = this.xpaths[i];
		if (this.path.isEmpty()) return false;
		this.next = 0;
		try {
			getNext();
			axis();
			path = null;
			return true;
		} catch (XPathException e) {
			path = null;
			return false;
		}
	}
	
	// Exception class to be thrown if xpath is malformed
	private class XPathException extends Exception {
	    public XPathException() {
	        super("Invalid XPath");
	    }
	    
	    public XPathException(String message) {
	        super(message);
	    }
	}
	
	// Function to get the next character in the path
	// Skips over whitespace or not based on argument
	private void getNext(boolean whitespace) {
		if (this.next == this.path.length()) {
			this.next = -1; // end
		} else {
			this.c = this.path.charAt(this.next);
			this.next += 1;
			// only ignore whitespace if "whitespace" is true
			if (whitespace) {
				if (Character.isWhitespace(this.c)) {
					getNext();
				}
			}
		}
	}
	
	// Default value for getNext (skip whitespace)
	private void getNext() {
		getNext(true);
	}
	
	// Checks for an expected "word"
	// if the word is found, pointer is at the end of the word
	// else the pointer is reset 
	private boolean isNextWord(String[] words) {
//		System.out.println("next word: " + words[0]);
		int temp = this.next;
		for (int w=0; w < words.length; w++) {
			String word = words[w];
			
			// Read word (no whitespace allowed except after last character)
			for (int i=0; i < word.length(); i++){
				getNext(i == word.length() - 1); 
				if (this.next == -1 || this.c != word.charAt(i)) {
					this.next = temp;
					return false;
				}
			}
		}
		return true;
	}
	
	//
	// Methods to parse different path components
	//
	
	// axis --> '/'
	private void axis() throws XPathException {
//		System.out.println("axis");
		if (this.c == '/'){ // further axis
			getNext();
			step();
		} else if (this.c == ']') { // end of step within test
			return;
		} else if (this.next == -1) { // end
			 return;
		} else {
			throw new XPathException();
		}
	}
	
	// test --> step
	//      --> text() = "..."
	//      --> contains(text(), "...")
	//      --> @attname = "..."
	private void test() throws XPathException {
//		System.out.println("test");
		if (isNextWord(new String[]{"text","(",")","=","\""})) text();
		else if (isNextWord(new String[]{"contains","(","text","(",")",",","\""})) contains();
		else if (isNextWord(new String[]{"@"})) attr();
		else step();
	}
	
	private void text() throws XPathException {
//		System.out.println("text");
		getNext();
		while(this.c != '"' && this.next != -1) { // TODO: check valid chars
			getNext();
		}
		if (this.c != '"') throw new XPathException();
		getNext();
	}
	
	private void contains() throws XPathException {
//		System.out.println("contains");
		getNext();
		while(this.c != '"' && this.next != -1) { // TODO: check valid chars
			getNext();
		}
		if (this.c != '"') throw new XPathException();
		getNext();
		if (this.c != ')') throw new XPathException();
		getNext();
	}
	
	private void attr() throws XPathException {
//		System.out.println("attr");
		while(this.c != '=' && this.next != -1) { // TODO: check valid chars
			getNext();
		}
		if (this.c != '=') throw new XPathException();
		getNext();
		if (this.c != '"') throw new XPathException();
		getNext();
		while(this.c != '"' && this.next != -1) { // TODO: check valid chars
			getNext();
		}
		if (this.c != '"') throw new XPathException();
		getNext();
		
	}
	
	// step --> nodename ([ test ])* (axis step)?s
	private void step() throws XPathException {
//		System.out.println("step");
		nodename();
		if (this.next != -1) {
			while (this.c == '[') {
				test();
				if (this.c != ']') throw new XPathException();
				getNext();
			}
			axis();
		}
	}
	
	private void nodename() throws XPathException {
		// -- Element names are case-sensitive
		// -- Element names must start with a letter or underscore
		// TODO: Element names cannot start with the letters xml (or XML, or Xml, etc)
		// -- Element names can contain letters, digits, hyphens, underscores, and periods
		// -- Element names cannot contain spaces
		
		// first character of a nodename must be a letter or underscore
		if (!Character.isAlphabetic(this.c) && !(this.c == '_')) throw new XPathException();
		
		// name CAN contain letters, digits, hyphens, underscores, and periods
		// name CANNOT contain spaces
		while (!Character.isWhitespace(this.c) && (this.next != -1) &&
			   (Character.isAlphabetic(this.c) || 
				Character.isDigit(this.c) || 
				this.c == '-' || this.c == '_' || this.c == '.')) {
			getNext();
		}
	}
	
	
	//
	// Evaluate and helpers
	//
  
	public boolean[] evaluate(Document d) { 
		if (d == null) return null;
		boolean[] evals = new boolean[this.xpaths.length];
		for (int i = 0; i < this.xpaths.length; i++) {
			if (!isValid(i)) { 
				// If the ith xpath is invalid, 
				// it automatically evaluates to false
				evals[i] = false; 
			} else {
				String path = this.xpaths[i];
				
				if (path.indexOf('/') == 0) path = path.substring(1);
	
				Node node = d.getDocumentElement();
				evals[i] = compare(path, node, true);
			}
		}
		return evals; 
  	}
	
	// Given a step, returns any tests within the step
	public ArrayList<String> getTests(String step) {
		// TODO: handle unmatched brackets within a string
		ArrayList<String> tests = new ArrayList<String>();
		
		if (step.indexOf('[') != -1) {
			StringBuilder s = new StringBuilder();
			int depth = 0;
			for (int i = step.indexOf('['); i < step.length(); i++) {
				char c = step.charAt(i);
				if (c == '[') {
					if (depth > 0) {
						s.append(c); // nested bracket
					}
					depth ++;
				} else if (c == ']') {
					depth --;
					// if closing bracket is not nested
					// add test to the return list
					if (depth == 0) {
						tests.add(s.toString());
						s = new StringBuilder();
					} else {
						s.append(c); // nested bracket
					}
				} else {
					s.append(c);
				}
			}
		} 
		return tests;
	}
	
	// Given a test string determine which type of grammar it is
	private Test getTestType(String test) {
		if (test.indexOf("text(") == 0) return Test.TEXT;
		else if (test.indexOf("contains(") == 0) return Test.CONTAINS_TEXT;
		else if (test.indexOf("@")  == 0) return Test.ATTRIBUTE;
		else return Test.STEP;
	}
	
	private enum Test {
		TEXT, CONTAINS_TEXT, ATTRIBUTE, STEP;
	}
		
	// Recursively explores Document matching to the given xpath
	public boolean compare(String path, Node node, boolean init){
		// Get the next step in the given path
		String[] stepPath = getStep(path);
		String step = stepPath[0];
		String newPath = stepPath[1];
		
//		System.out.println("Compare: " + (!step.isEmpty() ? step : "<step>") + " " + (!newPath.isEmpty() ? newPath : "<path>"));
		
		// Check if this step has tests
		ArrayList<String> tests = getTests(step);
		if (tests.size() > 0) {
			step = step.substring(0, step.indexOf('[')).trim();
		}
		while (!step.isEmpty()){
			// Determine if the step name matches an 
			// immediate descendant of the current node
			NodeList l = node.getChildNodes();
			for (int i = 0; i < l.getLength(); i++){
				Node child = l.item(i);
				// Deal with root node case
				if (init && node.getNodeName().equals(step)) {
					if (tests.size() > 0) {
						for (int t = 0; t < tests.size(); t++) {
							if (compareTest(tests.get(t), node)) {
								tests.remove(t);
							}
						}
						if (tests.isEmpty() && compare(newPath, node)) return true;
					} else {
						if (compare(newPath, node)) return true;
					}
				} else if (child.getParentNode().equals(node) && child.getNodeName().equals(step)) {
					if (tests.size() > 0) {
						// Check that all tests match some valid child node
						for (int t = 0; t < tests.size(); t++) {
							if (compareTest(tests.get(t), child)) {
								tests.remove(t);
							}
						}
						if (tests.isEmpty() && compare(newPath, child)) return true;
					} else {
						if (compare(newPath, child)) return true;
					}
				}
			}
			return false;
		}
		return true;
	}
	
	public boolean compare(String path, Node node) {
		return compare(path, node, false);
	}
	
	// Checks if a given node satisfies a given test
	public boolean compareTest(String t, Node node) {
		switch(getTestType(t)) {
		case TEXT: 
			String text = t.substring(t.indexOf("\"") + 1, t.lastIndexOf("\""));
			String nodeText = node.getTextContent();
			return (nodeText != null && nodeText.equals(text));
		case CONTAINS_TEXT: 
			String contains = t.substring(t.indexOf("\"") + 1, t.lastIndexOf("\""));
			String nodeContains = node.getTextContent();
			return (nodeContains != null && nodeContains.indexOf(contains) != -1);
		case ATTRIBUTE:
			String attr = t.substring(t.indexOf("@") + 1, t.indexOf("=")).trim();
			String attrVal = t.substring(t.indexOf("\"") + 1, t.lastIndexOf("\""));
			Node attrNode = node.getAttributes().getNamedItem(attr);
			return (attrNode != null && attrNode.getNodeValue().equals(attrVal));
		case STEP: 
			return compare(t, node);
		default: return false;
		}
	}
	
	// Get next step and remaining path
	public String[] getStep(String path) {
		if (path == null || path.length() == 0) return new String[]{"",""};
		boolean quote = false;
		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			if (c == '/' && !quote) { // axis
				return new String[] {path.substring(0, i), path.substring(i + 1)};
			} else if (c == '"' && path.charAt(i - 1) != '\\') { // non-escaped quote
				quote = !quote;
			}
		}
		// the remainder of the path is one step
		return new String[]{path, ""};
	}
        
}
