package com.tumri.joz.server.domain;

import java.util.HashSet;

/**
 * Used for JozQAReporting.  Provides a layer of abstraction at the Advertiser Level.  Grouping various
 * QA reporting information together by advertiser/clientName.
 * User: scbraun
 * Date: Oct 13, 2008
 * Time: 10:55:40 AM
 */
public class QAAdvertiserResponse {
	private int numFailedRecipes = 0;
	private int numSuccessfulRecipes = 0;
	private int numWarnedRecipes = 0;
	private String advertiserName = "";
	private boolean completeSuccess = true;
	private int numFailedTspecs = 0;
	private int numWarnTSpecs = 0;
	private HashSet<QARecipeResponse> warnedRecipeResponses= new HashSet<QARecipeResponse>();
	private HashSet<QARecipeResponse> failedRecipeResponses= new HashSet<QARecipeResponse>();

	public int getNumFailedTspecs() {
		return numFailedTspecs;
	}

	public void setNumFailedTspecs(int numFailedTspecs) {
		this.numFailedTspecs = numFailedTspecs;
	}

	public int getNumFailedRecipes() {
		return numFailedRecipes;
	}

	public void setNumFailedRecipes(int numFailedRecipes) {
		this.numFailedRecipes = numFailedRecipes;
	}

	public void incrementFailedRecipes(){
		numFailedRecipes++;
	}

	public void incrementWarnedRecipes(){
		numWarnedRecipes++;
	}

	public int getNumSuccessfulRecipes() {
		return numSuccessfulRecipes;
	}

	public void setNumSuccessfulRecipes(int numSuccessfulRecipes) {
		this.numSuccessfulRecipes = numSuccessfulRecipes;
	}

	public int getNumWarnedRecipes() {
		return numWarnedRecipes;
	}

	public void setNumWarnedRecipes(int numWarnedRecipes) {
		this.numWarnedRecipes = numWarnedRecipes;
	}

	public int getNumWarnTSpecs() {
		return numWarnTSpecs;
	}

	public void setNumWarnTSpecs(int numWarnTSpecs) {
		this.numWarnTSpecs = numWarnTSpecs;
	}

	public HashSet<QARecipeResponse> getWarnedRecipeResponses() {
		return warnedRecipeResponses;
	}

	public void setWarnedRecipeResponses(HashSet<QARecipeResponse> warnedRecipeResponses) {
		this.warnedRecipeResponses = warnedRecipeResponses;
	}

	public void incrementSuccessfulRecipes(){
		numSuccessfulRecipes++;
	}

	public String getAdvertiserName() {
		return advertiserName;
	}

	public void setAdvertiserName(String advertiserName) {
		this.advertiserName = advertiserName;
	}

	public boolean isCompleteSuccess() {
		return completeSuccess;
	}

	public void setCompleteSuccess(boolean completeSuccess) {
		this.completeSuccess = completeSuccess;
	}

	public HashSet<QARecipeResponse> getFailedRecipeResponses() {
		return failedRecipeResponses;
	}

	public void setFailedRecipeResponses(HashSet<QARecipeResponse> failedRecipeResponses) {
		this.failedRecipeResponses = failedRecipeResponses;
	}

	public void addFailedRecipeResponse(QARecipeResponse failedRecipeResp){
		if(failedRecipeResponses == null){
			failedRecipeResponses = new HashSet<QARecipeResponse>();
		}
		failedRecipeResponses.add(failedRecipeResp);
	}

	public void addWarnedRecipeResponse(QARecipeResponse warnedRecipeResp){
		 if(warnedRecipeResponses == null){
			warnedRecipeResponses = new HashSet<QARecipeResponse>();
		}
		warnedRecipeResponses.add(warnedRecipeResp);
	}
}
