package com.tumri.joz.targeting;

import com.tumri.cma.domain.*;
import com.tumri.joz.rules.ListingClause;

import java.util.HashMap;
import java.util.List;

/**
 * Container class to hold the result of the targeting
 */
public class TargetingResults {

    private Recipe recipe = null;
    private String[] camDimensionNames = null;
    private CAMDimensionType[] camDimensionTypes = null;
    private String[] attributeValues = null;
    private int[] attributePositions = null;
    private HashMap<String, String> fixedDimMap = null;


    List<RecipeTSpecInfo> infoListRecipe = null;
    List<ExperienceTSpecInfo> infoListExperience = null;
    ListingClause listingClause = null;
    private Experience experience = null;

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
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

    public List<RecipeTSpecInfo> getInfoListRecipe() {
        return infoListRecipe;
    }

    public void setInfoListRecipe(List<RecipeTSpecInfo> infoListRecipe) {
        this.infoListRecipe = infoListRecipe;
    }

    public List<ExperienceTSpecInfo> getInfoListExperience() {
        return infoListExperience;
    }

    public void setInfoListExperience(List<ExperienceTSpecInfo> infoListExperience) {
        this.infoListExperience = infoListExperience;
    }

    public ListingClause getListingClause() {
        return listingClause;
    }

    public void setListingClause(ListingClause listingClause) {
        this.listingClause = listingClause;
    }

    public Experience getExperience() {
        return experience;
    }

    public void setExperience(Experience experience) {
        this.experience = experience;
    }

    public HashMap<String, String> getFixedDimMap() {
        return fixedDimMap;
    }

    public void setFixedDimMap(HashMap<String, String> fixedDimMap) {
        this.fixedDimMap = fixedDimMap;
    }
}
