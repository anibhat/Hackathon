package com.example.hackathon.model;

public class Git {
	
	private String repository;
	
	private String userName;
	
	private String password;

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Git(String repository, String userName, String password) {
		super();
		this.repository = repository;
		this.userName = userName;
		this.password = password;
	}

	public Git() {
	}
	
	

}
