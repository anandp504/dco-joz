package com.tumri.joz.index.creator;

import java.io.Serializable;

/**
 * Container class to represent a provider index
 * @author: nipun
 * Date: Feb 15, 2008
 * Time: 1:30:38 PM
 */
public class PersistantProviderIndex implements Serializable {
    
    static final long serialVersionUID = 1L; 
    private String providerName;
    private PersistantIndex[] indices;

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public PersistantIndex[] getIndices() {
        return indices;
    }

    public void setIndices(PersistantIndex[] indices) {
        this.indices = indices;
    }
}
