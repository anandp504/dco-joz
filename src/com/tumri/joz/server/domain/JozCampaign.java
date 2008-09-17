package com.tumri.joz.server.domain;

import java.util.ArrayList;

public class JozCampaign {
	private String name=null;
	private String id=null;
	private String 		clientId;
    private String 		clientName;
	private ArrayList<JozAdPod> adpods = null;
	public ArrayList<JozAdPod> getAdpods() {
		return adpods;
	}
	public void setAdpods(ArrayList<JozAdPod> adpods) {
		this.adpods = adpods;
	}
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
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
}
