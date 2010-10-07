package com.tumri.joz.productselection;

import com.tumri.cma.domain.CAMDimensionType;
import com.tumri.cma.domain.Experience;
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
    Experience targetedExperience = null;

    private String[] camDimensionNames = null;
    private CAMDimensionType[] camDimensionTypes = null;
    private String[] attributeValues = null;
    private int[] attributePositions = null;
    private HashMap<String, String> fixedDimMap = null;

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

    public String[] getCamDimensionNames() {
        return camDimensionNames;
    }

    public void setCamDimensionNames(String[] camDimensionNames) {
        this.camDimensionNames = camDimensionNames;
    }

    public CAMDimensionType[] getCamDimensionTypes() {
        return camDimensionTypes;
    }

    public void setCamDimensionTypes(CAMDimensionType[] camDimensionTypes) {
        this.camDimensionTypes = camDimensionTypes;
    }

    public String[] getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(String[] attributeValues) {
        this.attributeValues = attributeValues;
    }

    public int[] getAttributePositions() {
        return attributePositions;
    }

    public void setAttributePositions(int[] attributePositions) {
        this.attributePositions = attributePositions;
    }

    public Experience getTargetedExperience() {
        return targetedExperience;
    }

    public void setTargetedExperience(Experience targetedExperience) {
        this.targetedExperience = targetedExperience;
    }

    public HashMap<String, String> getFixedDimMap() {
        return fixedDimMap;
    }

    public void setFixedDimMap(HashMap<String, String> fixedDimMap) {
        this.fixedDimMap = fixedDimMap;
    }
}
