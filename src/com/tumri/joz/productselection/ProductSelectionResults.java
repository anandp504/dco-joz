package com.tumri.joz.productselection;

import com.tumri.cma.domain.Recipe;
import com.tumri.joz.products.Handle;

import java.util.ArrayList;
import java.util.HashMap;

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
	ArrayList<Integer> idOrder = null;

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
        if(idOrder == null){
	        idOrder = new ArrayList<Integer>();
        }
	    tspecResultsMap.put(tspecId, results);
        tspecSlotIdMap.put(tspecId, slotId);
	    idOrder.add(tspecId);

    }

	public ArrayList<Integer> getIdOrder(){
		return idOrder;
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
