package com.tumri.joz.server.domain;

import java.util.ArrayList;

/**
 * Joz returns a JozQAResponse object.
 * The response will include a List of QAAdvertiserResponse(s).
 * Each QAAdvertiserResponse contains information about specific Advertisers(name, number of failed Recipes,
 * number of Successful Recipe, and a List of QARecipeResponse(s)).
 * QARecipeResponse will contain information about a failed Recipe(a list of failed TSpec(s), and a List of JozQAError(s)).
 * JozQAError will contain specific information about a given failed TSpec(number of request products, number of recieved products, and campaign information).
 * User: scbraun
 * Date: Oct 10, 2008
 * Time: 2:35:01 PM
 */
public class JozQAResponse {

	private ArrayList<QAAdvertiserResponse> advertiserInfos = new ArrayList<QAAdvertiserResponse>();
	private boolean success = true;
	//private String details = "";
	private ArrayList<String> details = new ArrayList<String>();
	private int totalNumSuccessRecieps = 0;
	private int totalNumFailedRecipes = 0;
	private int totalNumWarnRecipes = 0;

	/**
	 * used internally by JozQARequestHandler to process request
	 * @param advertiserName String
	 */
	public void registerRecipeSuccess(String advertiserName){
		int index = getAdvertiserIndex(advertiserName);
		totalNumSuccessRecieps += 1;

		if(index == -1){//advertiser not found within list
			QAAdvertiserResponse advertiserInfo = new QAAdvertiserResponse();
			advertiserInfo.setAdvertiserName(advertiserName);
			advertiserInfo.incrementSuccessfulRecipes();
			advertiserInfos.add(advertiserInfo);
		} else { //advertiser found within list
			QAAdvertiserResponse advertiserInfo = advertiserInfos.get(index);
			advertiserInfo.incrementSuccessfulRecipes();
		}

	}

	/**
	 * used internally by JozQARequestHandler to process request
	 * @param advertiserName String
	 * @param recipeResponse QARecipeResponse
	 */
	public void registerRecipeFailure(String advertiserName, QARecipeResponse recipeResponse){
		int index = getAdvertiserIndex(advertiserName);
		totalNumFailedRecipes += 1;
		if(index == -1){//advertiser not found within list
			QAAdvertiserResponse advertiserInfo = new QAAdvertiserResponse();
			advertiserInfo.incrementFailedRecipes();
			advertiserInfo.setAdvertiserName(advertiserName);
			advertiserInfo.addFailedRecipeResponse(recipeResponse);
			advertiserInfo.setCompleteSuccess(false);
			advertiserInfo.setNumFailedTspecs(recipeResponse.getNumFailedTSpecs());
			advertiserInfos.add(advertiserInfo);
		} else { //advertiser found within list
			QAAdvertiserResponse advertiserInfo = advertiserInfos.get(index);
			advertiserInfo.incrementFailedRecipes();
			advertiserInfo.addFailedRecipeResponse(recipeResponse);
			advertiserInfo.setCompleteSuccess(false);
			advertiserInfo.setNumFailedTspecs(advertiserInfo.getNumFailedTspecs() + recipeResponse.getNumFailedTSpecs());
		}
	}

	/**
	 * used internally by JozQARequestHandler to process request
	 * @param advertiserName String
	 * @param recipeResponse QARecipeResponse
	 */
	public void registerRecipeWarning(String advertiserName, QARecipeResponse recipeResponse){
		int index = getAdvertiserIndex(advertiserName);
		totalNumWarnRecipes += 1;
		if(index == -1){//advertiser not found within list
			QAAdvertiserResponse advertiserInfo = new QAAdvertiserResponse();
			advertiserInfo.incrementWarnedRecipes();
			advertiserInfo.setAdvertiserName(advertiserName);
			advertiserInfo.addWarnedRecipeResponse(recipeResponse);
			advertiserInfo.setNumWarnTSpecs(recipeResponse.getNumWarnedTSpecs());
			advertiserInfos.add(advertiserInfo);
		} else { //advertiser found within list
			QAAdvertiserResponse advertiserInfo = advertiserInfos.get(index);
			advertiserInfo.incrementWarnedRecipes();
			advertiserInfo.addWarnedRecipeResponse(recipeResponse);
			advertiserInfo.setNumWarnTSpecs(advertiserInfo.getNumWarnTSpecs() + recipeResponse.getNumWarnedTSpecs());
		}

	}

	private int getAdvertiserIndex(String advertiserName){
		int index = -1;
		//find index of advertiser; add one to list if not there
		for(int i = 0; i < advertiserInfos.size(); i++){
			QAAdvertiserResponse advertiserInfo = advertiserInfos.get(i);
			if(advertiserName.equals(advertiserInfo.getAdvertiserName())){
				index = i;
			}
		}

		return index;
	}

	public int getTotalNumWarnRecipes() {
		return totalNumWarnRecipes;
	}

	public void setTotalNumWarnRecipes(int totalNumWarnRecipes) {
		this.totalNumWarnRecipes = totalNumWarnRecipes;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public ArrayList<QAAdvertiserResponse> getAdvertiserInfos() {
		return advertiserInfos;
	}

	public void setAdvertiserInfos(ArrayList<QAAdvertiserResponse> advertiserInfos) {
		this.advertiserInfos = advertiserInfos;
	}

	public int getTotalNumSuccessRecieps() {
		return totalNumSuccessRecieps;
	}

	public void setTotalNumSuccessRecieps(int totalNumSuccessRecieps) {
		this.totalNumSuccessRecieps = totalNumSuccessRecieps;
	}

	public int getTotalNumFailedRecipes() {
		return totalNumFailedRecipes;
	}

	public void setTotalNumFailedRecipes(int totalNumFailedRecipes) {
		this.totalNumFailedRecipes = totalNumFailedRecipes;
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

	public ArrayList<String> getDetails() {
		return details;
	}

	public void setDetails(ArrayList<String> details) {
		this.details = details;
	}
}
