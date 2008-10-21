package com.tumri.joz.server.domain;

import com.tumri.cma.domain.TSpec;

import java.util.HashSet;
import java.util.ArrayList;

/**
 * This class is used to generate the JozQAReport.
 * It provides a layer of abstraction between advertiser lvl and listing query lvl.
 * User: scbraun
 * Date: Oct 15, 2008
 * Time: 12:31:45 PM
 */
public class QARecipeResponse {
	private int recipeId = -1;
	private String recipeName = "";
	private HashSet<TSpec> failedTSpecs = new HashSet<TSpec>();
	private HashSet<TSpec> warnedTSpecs = new HashSet<TSpec>();
	private HashSet<JozQAError> jozQAErrors = new HashSet<JozQAError>();
	private HashSet<JozQAError> jozQAWarnings = new HashSet<JozQAError>();
	private ArrayList<String> details = new ArrayList<String>();

	public ArrayList<String> getDetails() {
		return details;
	}

	public void setDetails(ArrayList<String> details) {
		this.details = details;
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

	public HashSet<TSpec> getFailedTSpecs() {
		return failedTSpecs;
	}

	public void setFailedTSpecs(HashSet<TSpec> failedTSpecs) {
		this.failedTSpecs = failedTSpecs;
	}

	public HashSet<JozQAError> getJozQAErrors() {
		return jozQAErrors;
	}

	public void setJozQAErrors(HashSet<JozQAError> jozQAErrors) {
		this.jozQAErrors = jozQAErrors;
	}

	public void addFailedTSpec(TSpec tSpec){
		failedTSpecs.add(tSpec);
	}

	public void addFailedTSpecs(HashSet<TSpec> tSpecList){
		for(TSpec tSpec: tSpecList){
			addFailedTSpec(tSpec);
		}
	}

	public void addJozQAError(JozQAError error){
		jozQAErrors.add(error);
	}

	public void addJozQAErrors(HashSet<JozQAError> errorsList){
		for(JozQAError error: errorsList){
			addJozQAError(error);
		}
	}
	
	public int getNumFailedTSpecs(){
		return failedTSpecs.size();
	}

	public int getNumWarnedTSpecs(){
		return warnedTSpecs.size();
	}

	public HashSet<TSpec> getWarnedTSpecs() {
		return warnedTSpecs;
	}

	public void setWarnedTSpecs(HashSet<TSpec> warnedTSpecs) {
		this.warnedTSpecs = warnedTSpecs;
	}

	public HashSet<JozQAError> getJozQAWarnings() {
		return jozQAWarnings;
	}

	public void setJozQAWarnings(HashSet<JozQAError> jozQAWarnings) {
		this.jozQAWarnings = jozQAWarnings;
	}
	
	public void addWarnedTSpec(TSpec tSpec){
		warnedTSpecs.add(tSpec);
	}

	public void addWarnedTSpecs(HashSet<TSpec> tSpecList){
		for(TSpec tSpec: tSpecList){
			addWarnedTSpec(tSpec);
		}
	}

	public void addJozQAWarning(JozQAError error){
		jozQAWarnings.add(error);
	}

	public void addJozQAWarnings(HashSet<JozQAError> errorsList){
		for(JozQAError error: errorsList){
			addJozQAWarning(error);
		}
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
