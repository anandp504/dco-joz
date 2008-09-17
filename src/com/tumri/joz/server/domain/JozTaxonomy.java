package com.tumri.joz.server.domain;

/**
 * JozTaxonomy class holds the results of a GetTaxonomy call.
 * 
 */
public class JozTaxonomy {
    JozCategory category = null;
    public void setRootCategory(JozCategory root){
    	category = root;
    }
	public JozCategory getRootCategory() {
		return category;
	}
}
