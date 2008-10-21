package com.tumri.joz.server.domain;

import com.tumri.cma.domain.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Oct 10, 2008
 * Time: 11:29:02 AM
 * This class is used to generate the JozQAReport.
 * It provides a the lowest layer of abstraction for recipes. Used by QARecipeResponse.
 * Details a TSpec within a Recipe that fails.
 */
public class JozQAError {
	private int numRequested = -1;
	private int numRecieved = -1;
	private boolean includedProducts = false;
	private boolean goeEnabled = false;
	private boolean allowExternalQuery = false;
	private int campId = -1;
	private String campName = "";
	private int adPodId = -1;
	private String adPodName = "";
	private int recipeId = -1;
	private String recipeName = "";
	private int tSpecId = -1;
	private String tSpecName = "";
	private ArrayList<String> details = new ArrayList<String>();

	public JozQAError(Campaign camp, AdPod adPod, Recipe recipe, TSpec tSpec, int numReqProducts){
		setCampId(camp.getId());
		setCampName(camp.getName());
		setAdPodId(adPod.getId());
		setAdPodName(adPod.getName());
		setRecipeId(recipe.getId());
		setRecipeName(recipe.getName());
		setTSpecId(tSpec.getId());
		setTSpecName(tSpec.getName());
		setGoeEnabled(tSpec.isGeoEnabledFlag() || tSpec.isApplyGeoFilter());
		List<ProductInfo> inclProds = tSpec.getIncludedProducts();
		if(inclProds != null && inclProds.size() > 0){
			setIncludedProducts(true);
		}
		setAllowExternalQuery(tSpec.isAllowExternalQuery());
		setNumRequested(numReqProducts);
	}

	public ArrayList<String> getDetails() {
		return details;
	}

	public void setDetails(ArrayList<String> details) {
		this.details = details;
	}

	public boolean isAllowExternalQuery() {
		return allowExternalQuery;
	}

	public void setAllowExternalQuery(boolean allowExternalQuery) {
		this.allowExternalQuery = allowExternalQuery;
	}

	public boolean isIncludedProducts() {
		return includedProducts;
	}

	public void setIncludedProducts(boolean includedProducts) {
		this.includedProducts = includedProducts;
	}

	public int getNumRequested() {
		return numRequested;
	}

	public void setNumRequested(int numRequested) {
		this.numRequested = numRequested;
	}

	public int getNumRecieved() {
		return numRecieved;
	}

	public void setNumRecieved(int numRecieved) {
		this.numRecieved = numRecieved;
	}

	public boolean isGoeEnabled() {
		return goeEnabled;
	}

	public void setGoeEnabled(boolean goeEnabled) {
		this.goeEnabled = goeEnabled;
	}

	public int getCampId() {
		return campId;
	}

	public void setCampId(int campId) {
		this.campId = campId;
	}

	public String getCampName() {
		return campName;
	}

	public void setCampName(String campName) {
		this.campName = campName;
	}

	public int getAdPodId() {
		return adPodId;
	}

	public void setAdPodId(int adPodId) {
		this.adPodId = adPodId;
	}

	public String getAdPodName() {
		return adPodName;
	}

	public void setAdPodName(String adPodName) {
		this.adPodName = adPodName;
	}

	public int getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(int recipeId) {
		this.recipeId = recipeId;
	}

	public String getRecipeName() {
		return recipeName;
	}

	public void setRecipeName(String recipeName) {
		this.recipeName = recipeName;
	}

	public int getTSpecId() {
		return tSpecId;
	}

	public void setTSpecId(int tSpecId) {
		this.tSpecId = tSpecId;
	}

	public String getTSpecName() {
		return tSpecName;
	}

	public void setTSpecName(String tSpecName) {
		this.tSpecName = tSpecName;
	}

	public void addDetail(String s){
		details.add(s);
	}

	public String getDetailsString(){
		String ret = null;
		for(String detail: details){
			if(ret == null){
				ret = "" + detail;
			} else {
				ret+=","+detail;
			}
		}
		return ret;
	}
}
