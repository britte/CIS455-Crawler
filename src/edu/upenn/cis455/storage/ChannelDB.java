package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class ChannelDB {
	
	private PrimaryIndex<String,Channel> channelByName;
	
	public ChannelDB(EntityStore store) {
		channelByName = store.getPrimaryIndex(String.class, Channel.class);
//		channelByCreator = store.getSecondaryIndex(channelByName, String.class, "creator");
	}
	
	public Channel getChannelByName(String name) {
		return channelByName.get(name);
	}
	
	public ArrayList<Channel> getallChannels() {
		ArrayList<Channel> channels;
		EntityCursor<Channel> iterChannels = channelByName.entities();
		try {
			channels = new ArrayList<Channel>();
			for (Channel c : iterChannels) {
				channels.add(c);
			}
		} finally {
			iterChannels.close();
		}
		return channels;
	}
	
	public boolean removeChannel(String name, String user) {
		Channel c = channelByName.get(name);
		if (c != null) {
			if (c.getCreator().equals(user)) {
				channelByName.delete(name);
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
//	public Channel[] getChannelsByCreator(String creator) {
//		return channelByCreator.g;
//	}
	
	// Inserts a channel into the store if it doesn't already exist
	// Returns true if channel was inserted, false if if existed already
	public boolean insertChannel(String name, String[] xpaths, String xsl, String creator) {
		if (channelByName.contains(name)) {
			return false;
		} else {
			channelByName.put(new Channel(name, xpaths, xsl, creator));
			return true;
		}
	}
}
