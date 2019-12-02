package com.example.hackathon.model;

public class Converter {

	private String dir;

	private Git git;
	
	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public Git getGit() {
		return git;
	}

	public void setGit(Git git) {
		this.git = git;
	}

	public Converter(String dir, Git git) {
		super();
		this.dir = dir;
		this.git = git;
	}

	public Converter() {
		super();
	}
	
	
}
