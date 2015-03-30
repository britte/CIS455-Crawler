package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.impl.Store;

public class DBWrapper {
	
	private static String envDirectory = null;
	
	private static Environment myEnv;
	private static EntityStore store;
	
	private static UserDB userDB;
	private static ChannelDB channelDB; 
	
	public DBWrapper(String envDir) throws Exception {
		
		this.envDirectory = envDir;
		
		setup();
		
		this.userDB = new UserDB(this.store);
		this.channelDB = new ChannelDB(this.store);
	}
	
	public Environment getEnvironment() { return this.myEnv; }
	public EntityStore getStore() { return this.store; }
	public UserDB getUserDB() { return this.userDB; }
	public ChannelDB getChannelDB() { return this.channelDB; }
	
	public void setup() {
		
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setTransactional(true);
		envConfig.setAllowCreate(true);
		File f = new File(this.envDirectory);
		if (!f.exists()) {
			f.mkdir();
		}
		this.myEnv = new Environment(new File(this.envDirectory), envConfig);
		
		StoreConfig storeConfig = new StoreConfig();
		storeConfig.setAllowCreate(true);
		storeConfig.setTransactional(true);
		this.store = new EntityStore(this.myEnv, "store", storeConfig);
	}
	
	public void close() {
		// Close store
		try {
			store.close();
		} catch(DatabaseException dbe) {
			System.err.println("Error closing store: " + dbe.toString());
			System.exit(-1);
		}
		 
		// Close environment
		try {
			myEnv.close();
		} catch(DatabaseException dbe) {
			System.err.println("Error closing MyDbEnv: " + dbe.toString());
			System.exit(-1);
		}
	}
	
}
