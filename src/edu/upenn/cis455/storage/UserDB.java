package edu.upenn.cis455.storage;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class UserDB {
	
	private PrimaryIndex<String,User> userByEmail;
	
	public UserDB(EntityStore store) {
		userByEmail = store.getPrimaryIndex(String.class, User.class);
	}
	
	public User getUserByEmail(String email) {
		return userByEmail.get(email);
	}
	
	// Inserts a user into the store if they don't already exist
	// Returns true if user was inserted, false if user existed already
	public boolean insertUser(String email, String pwd) {
		if (userByEmail.contains(email)) {
			return false;
		} else {
			userByEmail.put(new User(email, pwd));
			return true;
		}
	}
}
