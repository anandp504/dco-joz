package com.tumri.joz.productselection;

import java.util.ArrayList;

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
	//TODO: Add the campaign/ Adpod to this container, so we can get the Realm etc
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