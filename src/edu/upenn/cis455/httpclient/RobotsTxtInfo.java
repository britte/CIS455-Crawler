package edu.upenn.cis455.httpclient;

import java.util.ArrayList;
import java.util.HashMap;

public class RobotsTxtInfo {
	
	private HashMap<String,ArrayList<String>> disallowedLinks; // agent, links
	private HashMap<String,ArrayList<String>> allowedLinks; // agent, links
	
	private HashMap<String,Integer> crawlDelays; // agent, delays
	private ArrayList<String> sitemapLinks;
	private ArrayList<String> userAgents;
	
	private String rootUrl; 
	
	public RobotsTxtInfo(){
		disallowedLinks = new HashMap<String,ArrayList<String>>();
		allowedLinks = new HashMap<String,ArrayList<String>>();
		crawlDelays = new HashMap<String,Integer>();
		sitemapLinks = new ArrayList<String>();
		userAgents = new ArrayList<String>();
	}
	
	public RobotsTxtInfo(String url){
		disallowedLinks = new HashMap<String,ArrayList<String>>();
		allowedLinks = new HashMap<String,ArrayList<String>>();
		crawlDelays = new HashMap<String,Integer>();
		sitemapLinks = new ArrayList<String>();
		userAgents = new ArrayList<String>();
		rootUrl = url;
	}
	
	public void addDisallowedLink(String key, String value){
		if(!disallowedLinks.containsKey(key)){
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(value);
			disallowedLinks.put(key, temp);
		}
		else{
			ArrayList<String> temp = disallowedLinks.get(key);
			if(temp == null)
				temp = new ArrayList<String>();
			temp.add(value);
			disallowedLinks.put(key, temp);
		}
	}
	
	public void addAllowedLink(String key, String value){
		if(!allowedLinks.containsKey(key)){
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(value);
			allowedLinks.put(key, temp);
		}
		else{
			ArrayList<String> temp = allowedLinks.get(key);
			if(temp == null)
				temp = new ArrayList<String>();
			temp.add(value);
			allowedLinks.put(key, temp);
		}
	}
	
	public void addCrawlDelay(String key, Integer value){
		crawlDelays.put(key, value);
	}
	
	public void addSitemapLink(String val){
		sitemapLinks.add(val);
	}
	
	public void addUserAgent(String key){
		userAgents.add(key);
	}
	
	public boolean containsUserAgent(String key){
		return userAgents.contains(key);
	}
	
	public ArrayList<String> getDisallowedLinks(String key){
		return disallowedLinks.get(key);
	}
		
	public ArrayList<String> getAllowedLinks(String key){
		return allowedLinks.get(key);
	}
	
	public int getCrawlDelay(String key){
		return crawlDelays.get(key);
	}
	
	public String getRootUrl() {
		return rootUrl;
	}
	
	public void print(){
		for(String userAgent:userAgents){
			System.out.println("User-Agent: "+userAgent);
			ArrayList<String> dlinks = disallowedLinks.get(userAgent);
			if(dlinks != null)
				for(String dl:dlinks)
					System.out.println("Disallow: "+dl);
			ArrayList<String> alinks = allowedLinks.get(userAgent);
			if(alinks != null)
					for(String al:alinks)
						System.out.println("Allow: "+al);
			if(crawlDelays.containsKey(userAgent))
				System.out.println("Crawl-Delay: "+crawlDelays.get(userAgent));
			System.out.println();
		}
		if(sitemapLinks.size() > 0){
			System.out.println("# SiteMap Links");
			for(String sitemap:sitemapLinks)
				System.out.println(sitemap);
		}
	}
	
	public boolean crawlContainAgent(String key){
		return crawlDelays.containsKey(key);
	}
	
	public boolean checkLink(String key, String link) {
		ArrayList<String> allowed = getAllowedLinks(key);
		ArrayList<String> disallowed = getDisallowedLinks(key);
		
		String path = link.substring(rootUrl.length() - 1);
		
		for (int i=0; i < disallowed.size(); i++) {
			String dLink = disallowed.get(i);
			if (path.startsWith(dLink)) {
				//TODO: build in contradictions
//				for (int j=0; j < allowed.size(); j++ ) {
//					String aLink = allowed.get(j);
//					if (path.startsWith(aLink) && aLink.length() <= ) return true; 
//				}
				return false;
			}
		}
		return true;
	}
}
