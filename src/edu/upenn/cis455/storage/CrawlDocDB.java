package edu.upenn.cis455.storage;

import org.w3c.dom.Document;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class CrawlDocDB {
	
	private PrimaryIndex<String,CrawlDoc> docByUrl;

	public CrawlDocDB(EntityStore store) {
		docByUrl = store.getPrimaryIndex(String.class, CrawlDoc.class);
	}
	
	public CrawlDoc getDocByUrl(String url) {
		return docByUrl.get(url);
	}
	
	// Inserts a doc into the store if it doesn't already exist
	// Returns true if doc was inserted, false if if existed already
	public boolean insertCrawlDoc(String url, Document doc) {
		if (docByUrl.contains(url)) {
			return false;
		} else {
			docByUrl.put(new CrawlDoc(url, doc));
			return true;
		}
	}
	
	// Inserts a doc into the store if it doesn't already exist
	// Returns true if doc was inserted, false if if existed already
	public boolean insertCrawlDoc(String url, CrawlDoc doc) {
		if (docByUrl.contains(url)) {
			return false;
		} else {
			docByUrl.put(doc);
			return true;
		}
	}
}
