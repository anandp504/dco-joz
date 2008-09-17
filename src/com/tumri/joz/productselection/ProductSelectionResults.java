package com.tumri.joz.productselection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;

import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.Recipe;
import com.tumri.joz.products.Handle;

/**
 * Container class to hold the results of the Product Selection
 * @author nipun
 *
 */
public class ProductSelectionResults {

	HashMap<String, String> featuresMap = null;
    Recipe targetedRecipe = null;
    String targetedTSpecName = null;

    HashMap<Integer, ArrayList<Handle>> tspecResultsMap = null;
    HashMap<Integer, String> tspecSlotIdMap = null;

    public HashMap<String, String> getFeaturesMap() {
		return featuresMap;
	}
	
	public void setFeaturesMap(HashMap<String, String> featuresMap) {
		this.featuresMap = featuresMap;
	}

    public void addResults(Integer tspecId, String slotId, ArrayList<Handle> results) {
        if (tspecResultsMap == null) {
            tspecResultsMap = new HashMap<Integer, ArrayList<Handle>>();
        }
        if (tspecSlotIdMap == null) {
            tspecSlotIdMap = new HashMap<Integer, String>();
        }
        tspecResultsMap.put(tspecId, results);
        tspecSlotIdMap.put(tspecId, slotId);
    }

    public Recipe getTargetedRecipe() {
        return targetedRecipe;
    }

    public void setTargetedRecipe(Recipe targetedRecipe) {
        this.targetedRecipe = targetedRecipe;
    }

    public String getTargetedTSpecName() {
        return targetedTSpecName;
    }

    public void setTargetedTSpecName(String targetedTSpecName) {
        this.targetedTSpecName = targetedTSpecName;
    }

    public HashMap<Integer, ArrayList<Handle>> getTspecResultsMap() {
        return tspecResultsMap;
    }

    public HashMap<Integer, String> getTspecSlotIdMap() {
        return tspecSlotIdMap;
    }
}
