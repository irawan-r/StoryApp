package com.amora.storyapp.data.remote.model;

public class ListStoryItem{
	private String photoUrl;
	private String createdAt;
	private String name;
	private String description;
	private Object lon;
	private String id;
	private Object lat;

	public String getPhotoUrl(){
		return photoUrl;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public String getName(){
		return name;
	}

	public String getDescription(){
		return description;
	}

	public Object getLon(){
		return lon;
	}

	public String getId(){
		return id;
	}

	public Object getLat(){
		return lat;
	}
}
