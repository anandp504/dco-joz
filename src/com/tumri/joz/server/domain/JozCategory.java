package com.tumri.joz.server.domain;

import java.util.ArrayList;
import java.util.List;

public class JozCategory {
	private String name=null;
	private String glassIdStr = null;
	private String id=null;
	private String count = null;
	private List<JozCategory> categories = new ArrayList<JozCategory>();
    public void addChildren(JozCategory child){
    	categories.add(child);
    }
	public String getGlassIdStr() {
		return glassIdStr;
	}
	public void setGlassIdStr(String glassIdStr) {
		this.glassIdStr = glassIdStr;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<JozCategory> getChildren() {
		return categories;
	}
     
}
