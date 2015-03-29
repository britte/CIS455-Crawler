package edu.upenn.cis455.storage;

import java.util.Date;

import org.w3c.dom.Document;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class CrawlDoc {
	
	@PrimaryKey
	private String url;
		 
	private Document doc;
	private Date lastCrawled;
	
	public CrawlDoc() {}
	
	public CrawlDoc(String url, Document doc) {
		this.url = url; 
		this.doc = doc;
		this.lastCrawled = new Date();
	}
	
	public String getUrl() { return this.url; }
	public Document getDoc() { return this.doc; }
	public Date getLastCrawled() { return this.lastCrawled; }
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setDocument(Document doc) {
		this.doc = doc;
	}
	
	public void setLastCrawled() {
		this.lastCrawled = new Date();
	}
	
}
