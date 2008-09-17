package com.tumri.joz.server.domain;

import java.util.ArrayList;
import java.util.List;

public class JozCounts {
	private List<JozCategoryCount> category_count = new ArrayList<JozCategoryCount>();
    private List<JozBrandCount> brand_count = new ArrayList<JozBrandCount>();
    private List<JozProviderCount> provider_count = new ArrayList<JozProviderCount>();
    public void addCategoryCount(JozCategoryCount categoryCount){
    	category_count.add(categoryCount);
    }
    public void addBrandCount(JozBrandCount brandCount){
    	brand_count.add(brandCount);
    }
    public void addProviderCount(JozProviderCount providerCount){
    	provider_count.add(providerCount);
    }
	public List<JozBrandCount> getBrand() {
		return brand_count;
	}
	public List<JozCategoryCount> getCategory() {
		return category_count;
	}
	public List<JozProviderCount> getProvider() {
		return provider_count;
	}
	public void setBrand(List<JozBrandCount> brand) {
		this.brand_count = brand;
	}
	public void setCategory(List<JozCategoryCount> category) {
		this.category_count = category;
	}
	public void setProvider(List<JozProviderCount> provider) {
		this.provider_count = provider;
	}
}
