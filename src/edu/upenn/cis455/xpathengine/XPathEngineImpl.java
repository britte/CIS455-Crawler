package edu.upenn.cis455.xpathengine;

import org.w3c.dom.Document;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

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
	
	public class XPathException extends Exception {
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
    /* TODO: Check whether the document matches the XPath expressions */
    return null; 
  }
        
}
