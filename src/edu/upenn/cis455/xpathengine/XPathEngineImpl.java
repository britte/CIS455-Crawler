package edu.upenn.cis455.xpathengine;

import org.w3c.dom.*;

import javax.xml.parsers.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Stack;


public class XPathEngineImpl implements XPathEngine {
	
	String[] xpaths = new String[0];
	String path;
	int next;
	char c;

	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor!!
	}
	
	public void setXPaths(String[] s) {
		this.xpaths = s;
	}

	public boolean isValid(int i) {
		this.path = this.xpaths[i];
		if (this.path.isEmpty()) return false;
		this.next = 0;
		getNext();
		try {
			axis();
			return true;
		} catch (XPathException e) {
			return false;
		}
	}
	
	private class XPathException extends Exception {
	    public XPathException() {
	        super("Invalid XPath");
	    }
	    
	    public XPathException(String message) {
	        super(message);
	    }
	}
	
	private void getNext() {
		getNext(true);
	}
	
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
	
	// axis --> '/'
	private void axis() throws XPathException {
		if (this.c == '/'){
			step();
		} else if (this.next == -1) {
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
		if (isNextWord(new String[]{"text","(",")","=","\""})) text();
		else if (isNextWord(new String[]{"contains","(","text","(",")",",","\""})) contains();
		else if (isNextWord(new String[]{"@"})) attr();
		else step();
	}
	
	// Checks for an expected "word"
	// if the word is found, pointer is at the end of the word
	// else the pointer is reset 
	private boolean isNextWord(String[] words) {
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
	
	private void text() throws XPathException {
		getNext();
		while(this.c != '"' && this.next != -1) { // TODO: check valid chars
			getNext();
		}
		if (this.c != '"') throw new XPathException();
		getNext();
	}
	
	private void contains() throws XPathException {
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
	
	// step --> nodename ([ test ])* (axis step)?
	// TODO: * repeat
	private void step() throws XPathException {
		nodename();
		if (this.next != -1) {
			if (this.c == '[') {
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
		// Element names cannot start with the letters xml (or XML, or Xml, etc)
		// -- Element names can contain letters, digits, hyphens, underscores, and periods
		// -- Element names cannot contain spaces
				
		getNext();
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
				String[] steps = path.split("/");	
				// TODO: handle split characters within text
				Element root = d.getDocumentElement();
			}
		}
		return null; 
  	}
	
	// Given a step, returns any tests within the step
	public ArrayList<String> getTest(String step) {
		
		// TODO: handle brackets within a string
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
	
	private Test getTestType(String test) {
		if (test.indexOf("text(") == 0) return Test.TEXT;
		else if (test.indexOf("contains(") == 0) return Test.CONTAINS_TEXT;
		else if (test.indexOf("@")  == 0) return Test.ATTRIBUTE;
		else return Test.STEP;
	}
	
	private enum Test {
		TEXT, CONTAINS_TEXT, ATTRIBUTE, STEP;
	}
	
	public void compareStep(String step, Node node) {
		ArrayList<String> tests = getTest(step);
	}
	
	public boolean compareTests(ArrayList<String> tests, Node node) {
		for (String t : tests) {
			switch(getTestType(t)) {
			case TEXT: 
				String text = t.substring(t.indexOf("\"") + 1, t.lastIndexOf("\""));
				String nodeText = node.getTextContent();
				if (nodeText == null || !nodeText.equals(text)) return false;
				break;
			case CONTAINS_TEXT: 
				String contains = t.substring(t.indexOf("\"") + 1, t.lastIndexOf("\""));
				String nodeContains = node.getTextContent();
				if (nodeContains == null || !nodeContains.contains(contains)) return false;
				break;
			case ATTRIBUTE:
				String attr = t.substring(t.indexOf("@") + 1, t.indexOf("=")).trim();
				String attrVal = t.substring(t.indexOf("\"") + 1, t.lastIndexOf("\""));
				Node attrNode = node.getAttributes().getNamedItem(attr);
				if (attrNode == null || attrNode.getNodeValue().equals(attrVal)) return false;
				break;
			case STEP: break;
			}
		
		}
		return true;
	}
        
}
