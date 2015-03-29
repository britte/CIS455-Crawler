package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class User {
	@PrimaryKey
	private String email;
	
	private String pwd; 
	
	public User() {}
	
	public User(String email, String pwd) {
		this.email = email;
		this.pwd = pwd;
	}
	
	public String getEmail() { return this.email; }
	public String getPwd() { return this.pwd; }
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	
}
