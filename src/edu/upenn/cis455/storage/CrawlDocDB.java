package edu.upenn.cis455.storage;

import java.util.ArrayList;

import org.w3c.dom.Document;

import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
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
	
	public ArrayList<CrawlDoc> getallDocs() {
		ArrayList<CrawlDoc> docs;
		EntityCursor<CrawlDoc> iterDocs = docByUrl.entities();
		try {
			docs = new ArrayList<CrawlDoc>();
			for (CrawlDoc d : iterDocs) {
				docs.add(d);
			}
		} finally {
			iterDocs.close();
		}
		return docs;
	}
	
	// Inserts a doc into the store if it doesn't already exist
	// Returns true if doc was inserted, false if if existed already
	public boolean insertCrawlDoc(CrawlDoc doc, Transaction t) {
		return docByUrl.putNoOverwrite(t, doc);
	}
	
	// Updates a doc into the store if it doesn't already exist
	// Returns true if update was successful
	public void updateCrawlDoc(CrawlDoc doc, Transaction t) {
		doc.setLastCrawled();
		docByUrl.put(t, doc);
	}
}
