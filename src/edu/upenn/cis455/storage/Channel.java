package edu.upenn.cis455.storage;

import java.util.ArrayList;
import java.util.HashSet;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class Channel {
	
	@PrimaryKey
	private String name;
	
	@SecondaryKey(relate = Relationship.MANY_TO_ONE)
	private String creator;
	 
	private String[] xpaths;
	private String xsl;
	
	private HashSet<String> matchedUrls;
	
	public Channel() {}
	
	public Channel(String name, String[] xpaths, String xsl, String creator) {
		this.name = name; // TODO: fix primary key
		this.xpaths = xpaths;
		this.xsl = xsl;
		this.creator = creator;
		this.matchedUrls = new HashSet<String>();
	}
	
	public String getName() { return this.name; }
	public String[] getXPaths() { return this.xpaths; }
	public String getXsl() { return this.xsl; }
	public String getCreator() { return this.creator; }
	public HashSet<String> getMatched() { return this.matchedUrls; } 
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setXpaths(String[] xpaths) {
		this.xpaths = xpaths;
	}
	
	public void setXsl(String xsl) {
		this.xsl = xsl;
	}
	
	public void addMatch(String doc) {
		this.matchedUrls.add(doc);
	}
	
}
