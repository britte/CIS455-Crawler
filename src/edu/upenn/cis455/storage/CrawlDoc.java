package edu.upenn.cis455.storage;

import java.util.Date;

import org.w3c.dom.Document;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis455.httpclient.HttpResponse;

@Entity
public class CrawlDoc {
	
	@PrimaryKey
	private String url;
		 
	private String doc;
	private String docType;
	private long docLength;
	private Date lastCrawled;
	
	public CrawlDoc() {}
	
	public CrawlDoc(String url, String doc, String docType, long docLength) {
		this.url = url; 
		this.doc = doc;
		this.docType = docType;
		this.docLength = docLength;
		this.lastCrawled = new Date();
	}
	
	public String getUrl() { return this.url; }
	public Document getDoc() { return HttpResponse.toDoc(this.doc, this.docType); }
	public String getDocType() { return this.docType; }
	public long getDocLength() { return this.docLength; }
	public String getDocBody() { return this.doc.substring(this.doc.indexOf('>') + 1); }
	public Date getLastCrawled() { return this.lastCrawled; }
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setDocument(String doc, String docType) {
		this.doc = doc;
		this.docType = docType;
	}
	
	public void setLastCrawled() {
		this.lastCrawled = new Date();
	}
	
}
