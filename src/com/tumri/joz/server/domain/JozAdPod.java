package com.tumri.joz.server.domain;

import java.util.ArrayList;

public class JozAdPod {
	private String name=null;
	private String id=null;
	private String adType= null;
	private ArrayList<JozLocation> locations = null;
	private ArrayList<JozRecipe> recipes = null;
	public String getAdType() {
		return adType;
	}
	public void setAdType(String adType) {
		this.adType = adType;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ArrayList<JozLocation> getLocations() {
		return locations;
	}
	public void setLocations(ArrayList<JozLocation> locations) {
		this.locations = locations;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<JozRecipe> getRecipes() {
		return recipes;
	}
	public void setRecipes(ArrayList<JozRecipe> recipes) {
		this.recipes = recipes;
	}
}
