package com.tumri.joz.server.domain;

public class JozAdvertiser implements Comparable {
	private String name=null;
	private String id=null;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int compareTo(Object adv1){
		return name.compareTo(((JozAdvertiser)adv1).getName()) != 0 ? name.compareTo(((JozAdvertiser)adv1).getName()): id.compareTo(((JozAdvertiser)adv1).getId());
	}
	public boolean equals(Object adv1){
		return name.equals(((JozAdvertiser)adv1).getName()) && id.equals(((JozAdvertiser)adv1).getId());
	}
}
