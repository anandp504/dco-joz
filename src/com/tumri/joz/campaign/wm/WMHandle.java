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
 *
 * @author: nipun
 * Date: Aug 3, 2009
 * Time: 12:54:54 PM
 */
public class WMHandle implements Handle, Cloneable {

	private List<RecipeWeight> recipeList;
	private Map<WMAttribute, Integer> contextMap;
	private double score = 0.0;
	private double normFactor = 1.0;
	private long oid = 0;

	public WMHandle(long aOid, Map<WMAttribute, Integer> contextMap, List<RecipeWeight> recipeWeights) {
		this.oid = aOid;
		this.recipeList = recipeWeights;
		this.contextMap = contextMap;
		if (contextMap != null) {
			Set<WMAttribute> keys = contextMap.keySet();
			double n = 1.0;
			for (WMAttribute kAttr : keys) {
				double defWt = WMAttributeWeights.getDefaultAttributeWeight(kAttr);
				n += defWt * defWt;
			}
			this.normFactor = 1 / n;
		}

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

	public double getNormFactor() {
		return normFactor;
	}

	public void setNormFactor(double normFactor) {
		this.normFactor = normFactor;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WMHandle that = (WMHandle) o;
		if (oid != that.oid) return false;
		return true;
	}

	public int hashCode() {
		return (int) getOid();
	}


	public int compareTo(Object handle) {
		WMHandle ph = (WMHandle) handle;
		if (score > ph.score) return -1;
		if (score < ph.score) return 1;

		return (oid < ph.oid ? -1 :
				oid == ph.oid ? 0 : 1);
	}


	public int compare(Object h1, Object h2) {
		WMHandle handle1 = (WMHandle) h1;
		WMHandle handle2 = (WMHandle) h2;
		if (handle1.score > handle2.score) return 1;
		if (handle1.score < handle2.score) return -1;
		if (handle1.oid < handle2.oid) return -1;
		if (handle1.oid > handle2.oid) return 1;
		return 0;

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
		if (score == this.score) {
			return this;
		}
		WMHandle c = (WMHandle) this.clone();
		c.score = score;
		return c;
	}

	public Map<WMAttribute, Integer> getContextMap() {
		return contextMap;
	}
}
