package com.tumri.joz.productselection;

import java.util.ArrayList;
import java.util.HashMap;

import com.tumri.cma.domain.OSpec;
import com.tumri.joz.products.Handle;

/**
 * Container class to hold the results of the Product Selection
 * @author nipun
 *
 */
public class ProductSelectionResults {

	ArrayList<Handle> results = null;
	OSpec targetedOSpec = null;
	HashMap<String, String> featuresMap = null;
	
	public HashMap<String, String> getFeaturesMap() {
		return featuresMap;
	}
	
	public void setFeaturesMap(HashMap<String, String> featuresMap) {
		this.featuresMap = featuresMap;
	}

	public ArrayList<Handle> getResults() {
		return results;
	}
	
	public void setResults(ArrayList<Handle> results) {
		this.results = results;
	}

	public OSpec getTargetedOSpec() {
		return targetedOSpec;
	}
	
	public void setTargetedOSpec(OSpec targettedOSpec) {
		this.targetedOSpec = targettedOSpec;
	}
	
}
