package com.tumri.joz.server.domain;

public class JozCategoryCount {
	private String name=null;
	private String count=null;
	public JozCategoryCount(String nameVal,String countVal){
		name = nameVal;
		count = countVal;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
