/*
 * WMHandle.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.campaign.wm;

import com.tumri.joz.products.Handle;

import java.util.*;

/**
 * Weight matrix handle
 * @author: nipun
 * Date: Aug 3, 2009
 * Time: 12:54:54 PM
 */
public class WMHandle implements Handle, Cloneable {

    private List<RecipeWeight> recipeList;
    private Map<WMIndex.Attribute, Integer> contextMap;
    private double score = 0.0;
    private long oid = 0;

    public WMHandle(long aOid, Map<WMIndex.Attribute, Integer> contextMap, List<RecipeWeight> recipeWeights) {
        this.oid = aOid;
        this.recipeList = recipeWeights;
        this.contextMap = contextMap;
        Set<WMIndex.Attribute> keys = contextMap.keySet();
        int reqCount = keys.size();
        double score = 0.1;
        if (reqCount>0){
            score = 1/Math.sqrt(reqCount);
        }
        this.score = score;
    }

    public long getOid() {
        return oid;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double d) {
        score = d;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WMHandle that = (WMHandle) o;
        if (oid != that.oid) return false;
        return true;
    }

    public int hashCode() {
        return (int)getOid();
    }


    public int compareTo(Object handle) {
        WMHandle ph = (WMHandle)handle;
        if (score > ph.score) return -1;
        if (score < ph.score) return 1;

        return (oid < ph.oid ? -1 :
                oid == ph.oid ? 0 : 1);
    }


    public int compare(Object h1, Object h2) {
        WMHandle handle1 = (WMHandle)h1;
        WMHandle handle2 = (WMHandle)h2;
        if (handle1.score > handle2.score) return 1;
        if (handle1.score < handle2.score) return -1;
        if (handle1.oid < handle2.oid) return -1;
        if (handle1.oid > handle2.oid) return 1;
        return 0;

    }


    /**
     * Vector dot product of two normalized vectors
     *
     * @param rv a normalized vector for dot product
     * @return score returns score between 0 and 1
     */
    public double dot(WMHandle rv) {
        double score =0.0;
        if (contextMap.size()==0 || rv.contextMap.size()==0){
            return score;
        }
        Set<WMIndex.Attribute> keys = contextMap.keySet();
        for (WMIndex.Attribute currKey: keys){
            Integer inputVal = rv.contextMap.get(currKey);
            Integer wmVal = contextMap.get(currKey);
            if (inputVal==null || wmVal == null || !wmVal.equals(inputVal)) {
                //Note: Only select this if the WM vector is a proper subset to the input Vector
                score = 0.0;
                break;
            } else {
                score += this.getScore() * rv.getScore();
            }
        }
        return score;
    }

    public List<RecipeWeight> getRecipeList() {
        return recipeList;
    }

    public void setRecipeList(List<RecipeWeight> recipeList) {
        this.recipeList = recipeList;
    }

    public Object clone() {
        WMHandle copyVector = null;
        try {
            copyVector = (WMHandle) super.clone();
        }
        catch (CloneNotSupportedException e) {
            // this should never happen
            throw new RuntimeException(e.toString());
        }
        copyVector.contextMap = this.contextMap;
        copyVector.score = this.score;
        copyVector.oid = this.oid;
        return copyVector;
    }

    public Handle createHandle(double score) {
        throw new UnsupportedOperationException("This is not supported");
    }

    public Map<WMIndex.Attribute, Integer> getContextMap() {
        return contextMap;
    }
}
